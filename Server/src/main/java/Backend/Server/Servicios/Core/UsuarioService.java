package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Entidades.Enums.EstadoUsuario;
import Backend.Server.Repositorios.ConfiguracionLimitesRepository;
import Backend.Server.Repositorios.ConexionRepository;
import Backend.Server.Repositorios.UsuarioInfoDTO;
import Backend.Server.Repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConexionRepository conexionRepository;
    private final ConfiguracionLimitesRepository limitesRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario crearUsuario(String username, String email, String password) {

        Usuario usuario = Usuario.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void aceptarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEstado(EstadoUsuario.ACEPTADO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void rechazarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEstado(EstadoUsuario.RECHAZADO);
        usuarioRepository.save(usuario);
    }

    public List<Usuario> listarPendientes() {
        return usuarioRepository.findAllByEstado(EstadoUsuario.PENDIENTE);
    }

    public List<Usuario> listarAceptados() {
        return usuarioRepository.findAllByEstado(EstadoUsuario.ACEPTADO);
    }

    public boolean puedeConectarse(Long usuarioId) {

        ConfiguracionLimites limites = limitesRepository.findByActivaTrue()
                .orElseThrow(() -> new IllegalStateException("No hay configuración activa"));

        long conexionesActivas = conexionRepository
                .findByUsuarioIdAndActivaTrue(usuarioId)
                .size();

        return conexionesActivas < limites.getMaxConexionesPorUsuario();
    }

    public UsuarioInfoDTO obtenerInfoCompleta(Long usuarioId) {
        return usuarioRepository.findInfoCompletaById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public long contarTotal() {
        return usuarioRepository.count();
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

}
