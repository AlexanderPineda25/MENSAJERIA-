package Backend.Server.Utils.Dto;

public record MensajeListadoDTO(
        Long id,
        String remitente,
        String destinatario,
        String tipo,
        java.time.LocalDateTime fecha
) {}
