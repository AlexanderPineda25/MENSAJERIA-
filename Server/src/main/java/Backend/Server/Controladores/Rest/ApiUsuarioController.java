package Backend.Server.Controladores.Rest;

import Backend.Server.Servicios.Core.UsuarioService;
import Backend.Server.Repositorios.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class ApiUsuarioController {

    private final UsuarioService usuarioService;
    private final MensajeRepository mensajeRepository;

    @GetMapping("/{id}/info")
    public ResponseEntity<?> obtenerInfoCompleta(@PathVariable Long id) {

        var info = usuarioService.obtenerInfoCompleta(id);

        if (info == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(
                new UsuarioInfoResponse(
                        info.getId(),
                        info.getUsername(),
                        info.getEmail(),
                        info.getEstado(),
                        info.getConectado(),
                        info.getFechaRegistro(),
                        info.getUltimaConexion(),
                        info.getMensajesEnviados(),
                        info.getMensajesRecibidos()
                )
        );
    }

    @GetMapping("/{id}/enviados")
    public ResponseEntity<?> obtenerMensajesEnviados(@PathVariable Long id) {

        var lista = mensajeRepository.findMensajesEnviadosByUsuarioId(id)
                .stream()
                .map(m -> new MensajeEnviadoResponse(
                        m.getIpOrigen(),
                        m.getIpDestino(),
                        m.getFechaHora(),
                        m.getRemoto(),
                        m.getContenido()
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }


    @GetMapping("/{id}/recibidos")
    public ResponseEntity<?> obtenerMensajesRecibidos(@PathVariable Long id) {

        var lista = mensajeRepository.findMensajesRecibidosByUsuarioId(id)
                .stream()
                .map(m -> new MensajeRecibidoResponse(
                        m.getIpOrigen(),
                        m.getIpDestino(),
                        m.getFechaHora(),
                        m.getRemoto(),
                        m.getContenido()
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    record UsuarioInfoResponse(
            Long id,
            String username,
            String email,
            String estado,
            Boolean conectado,
            java.time.LocalDateTime fechaRegistro,
            java.time.LocalDateTime ultimaConexion,
            Long mensajesEnviados,
            Long mensajesRecibidos
    ) {}

    record MensajeEnviadoResponse(
            String ipOrigen,
            String ipDestino,
            LocalDateTime fechaHora,
            String remoto,
            String contenido
    ) {}


    record MensajeRecibidoResponse(
            String ipOrigen,
            String ipDestino,
            LocalDateTime fechaHora,
            String remoto,
            String contenido
    ) {}

    record MensajesResponse(Object mensajes) {}
}

