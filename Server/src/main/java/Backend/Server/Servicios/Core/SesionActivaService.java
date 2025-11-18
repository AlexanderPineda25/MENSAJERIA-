package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.SesionActiva;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Repositorios.SesionActivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SesionActivaService {

    private final SesionActivaRepository sesionRepository;

    @Transactional
    public SesionActiva crearSesion(Usuario usuario, String ip, String app, String version) {

        String token = UUID.randomUUID().toString();

        SesionActiva sesion = SesionActiva.builder()
                .usuario(usuario)
                .tokenSesion(token)
                .direccionIP(ip)
                .aplicacionCliente(app)
                .versionCliente(version)
                .activa(true)
                .build();

        return sesionRepository.save(sesion);
    }

    public SesionActiva buscarPorToken(String token) {
        return sesionRepository.findByTokenSesion(token).orElse(null);
    }

    @Transactional
    public void actualizarActividad(String token) {
        SesionActiva sesion = buscarPorToken(token);
        if (sesion != null && sesion.isActiva()) {
            sesion.setUltimaActividad(LocalDateTime.now());
            sesionRepository.save(sesion);
        }
    }

    @Transactional
    public void cerrarSesion(String token) {
        SesionActiva sesion = buscarPorToken(token);
        if (sesion != null) {
            sesion.setActiva(false);
            sesionRepository.save(sesion);
        }
    }

    @Transactional
    public void cerrarSesionesDeUsuario(Long usuarioId) {
        List<SesionActiva> sesiones = sesionRepository.findByUsuarioIdAndActivaTrue(usuarioId);
        sesiones.forEach(s -> s.setActiva(false));
        sesionRepository.saveAll(sesiones);
    }

    public List<SesionActiva> listarActivas() {
        return sesionRepository.findByActivaTrue();
    }

    /**
     * Limpia sesiones expiradas por inactividad.
     * Estructura ideal: cada 5 min expira sesiones con más de 30 min sin actividad.
     */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void limpiarSesionesExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(30);
        List<SesionActiva> expiradas = sesionRepository.findSesionesExpiradas(limite);

        expiradas.forEach(s -> s.setActiva(false));
        if (!expiradas.isEmpty()) {
            sesionRepository.saveAll(expiradas);
        }
    }
}
