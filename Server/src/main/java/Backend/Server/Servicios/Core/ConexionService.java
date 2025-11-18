package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.Conexion;
import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Entidades.Enums.EstadoUsuario;
import Backend.Server.Entidades.Enums.TipoAccion;
import Backend.Server.Entidades.RegistroAccion;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Repositorios.*;
import Backend.Server.Servicios.Observers.SistemaObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConexionService {

    private final ConexionRepository conexionRepository;
    private final SesionActivaRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionLimitesRepository limitesRepository;
    private final List<SistemaObserver> observers;

    @Transactional
    public Conexion iniciarConexion(Usuario usuario, String ip, String sistemaOperativo, String nombreDispositivo) {

        if (usuario.getEstado() != EstadoUsuario.ACEPTADO) {
            throw new IllegalStateException("Usuario no aceptado");
        }

        ConfiguracionLimites limites = limitesRepository.findByActivaTrue()
                .orElseThrow(() -> new IllegalStateException("No hay límites configurados"));

        long conexionesActivas = conexionRepository.findByUsuarioIdAndActivaTrue(usuario.getId()).size();

        if (conexionesActivas >= limites.getMaxConexionesPorUsuario()) {
            notificar(TipoAccion.LIMITE_ALCANZADO, usuario, "Límite de conexiones alcanzado", ip);
            throw new IllegalStateException("Límite de conexiones por usuario alcanzado");
        }

        Conexion conexion = Conexion.builder()
                .usuario(usuario)
                .direccionIP(ip)
                .sistemaOperativo(sistemaOperativo)
                .nombreDispositivo(nombreDispositivo)
                .build();

        conexionRepository.save(conexion);

        usuario.setConectado(true);
        usuario.setFechaUltimaConexion(LocalDateTime.now());
        usuarioRepository.save(usuario);

        notificar(TipoAccion.CONEXION, usuario, "Conexión iniciada desde " + ip, ip);
        return conexion;
    }

    @Transactional
    public void cerrarConexion(Conexion conexion) {

        conexion.cerrarSesion();
        conexionRepository.save(conexion);

        Usuario usuario = conexion.getUsuario();
        long conexionesActivas = conexionRepository.findByUsuarioIdAndActivaTrue(usuario.getId()).size();

        if (conexionesActivas == 0) {
            usuario.setConectado(false);
            usuarioRepository.save(usuario);
        }

        notificar(TipoAccion.DESCONEXION, usuario, "Conexión cerrada", conexion.getDireccionIP());
    }

    private void notificar(TipoAccion tipo, Usuario usuario, String descripcion, String ip) {

        RegistroAccion accion = RegistroAccion.builder()
                .tipo(tipo)
                .usuario(usuario)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .direccionIP(ip)
                .build();

        observers.forEach(o -> o.notificar(accion));
    }

    public long contarConexionesActivas() {
        return conexionRepository.findByActivaTrue().size();
    }
}
