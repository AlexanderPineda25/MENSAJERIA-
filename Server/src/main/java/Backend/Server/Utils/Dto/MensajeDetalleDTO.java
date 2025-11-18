package Backend.Server.Utils.Dto;

public record MensajeDetalleDTO(
        Long id,
        String remitente,
        String destinatario,
        String contenido,
        java.time.LocalDateTime fecha,
        String tipo,
        Long archivoId,
        String archivoNombre,
        Long archivoTamano
) {}
