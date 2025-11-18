package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Entidades.SesionActiva;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Entidades.Enums.EstadoUsuario;
import Backend.Server.Repositorios.ConfiguracionLimitesRepository;
import Backend.Server.Repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionLimitesRepository limitesRepository;
    private final SesionActivaService sesionActivaService;
    private final PasswordEncoder passwordEncoder;

    public SesionActiva login(String username, String password, String ip, String app, String version) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (usuario.getEstado() != EstadoUsuario.ACEPTADO) {
            throw new IllegalStateException("Usuario no aceptado");
        }

        ConfiguracionLimites limites = limitesRepository.findByActivaTrue()
                .orElseThrow(() -> new IllegalStateException("No hay configuración"));

        long sesionesActivas = sesionActivaService.listarActivas().stream()
                .filter(s -> s.getUsuario().getId().equals(usuario.getId()))
                .count();

        if (sesionesActivas >= limites.getMaxConexionesPorUsuario()) {
            throw new IllegalStateException("Límite de sesiones activas alcanzado");
        }

        return sesionActivaService.crearSesion(usuario, ip, app, version);
    }
}
