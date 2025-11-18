package frontend.client.network;

import Shared.Mensaje;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Wrapper para mostrar mensajes formateados en la interfaz.
 */
public class MensajeRecibido {

    private final Mensaje mensaje;
    private final boolean esPropio;

    public MensajeRecibido(Mensaje mensaje, boolean esPropio) {
        this.mensaje = mensaje;
        this.esPropio = esPropio;
    }

    public Mensaje getMensaje() {
        return mensaje;
    }

    public boolean isEsPropio() {
        return esPropio;
    }

    /**
     * Texto ya formateado para mostrar en la vista del chat
     */
    public String getTextoFormateado() {
        if (mensaje == null) return "";

        StringBuilder sb = new StringBuilder();

        // ⏰ Timestamp
        sb.append("[").append(getHoraFormateada()).append("] ");

        // 👤 remitente
        sb.append(esPropio ? "Tú: " : mensaje.getRemitente() + ": ");

        // 📎 Archivos
        if ("MENSAJE_ARCHIVO".equals(mensaje.getTipo())) {
            sb.append("📎 ").append(mensaje.getNombreArchivo());

            long size = mensaje.getTamanoArchivo();
            if (size > 0) {
                sb.append(" (").append(formatearTamano(size)).append(")");
            }

        } else {
            // 📝 Texto normal
            sb.append(mensaje.getContenido());
        }

        return sb.toString();
    }

    /**
     * Hora simple HH:mm
     */
    public String getHoraFormateada() {
        LocalDateTime fecha = mensaje.getFechaHora();
        if (fecha == null) fecha = LocalDateTime.now();

        return fecha.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Fecha completa dd/MM/yyyy HH:mm:ss
     */
    public String getFechaHoraCompleta() {
        LocalDateTime fecha = mensaje.getFechaHora();
        if (fecha == null) fecha = LocalDateTime.now();

        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    /**
     * Formato de tamaño de archivo (B, KB, MB)
     */
    private String formatearTamano(long bytes) {

        if (bytes < 1024)
            return bytes + " B";

        if (bytes < 1024 * 1024)
            return String.format("%.2f KB", bytes / 1024.0);

        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Validación de tipo archivo
     */
    public boolean esArchivo() {
        return "MENSAJE_ARCHIVO".equals(mensaje.getTipo());
    }

    /**
     * Validación de tipo texto
     */
    public boolean esTexto() {
        return "MENSAJE_TEXTO".equals(mensaje.getTipo());
    }

    /**
     * Estilos para UI (si deseas burbujas tipo WhatsApp)
     */
    public String getEstiloCSS() {
        return esPropio
                ? "-fx-background-color: #dcf8c6; -fx-padding: 5; -fx-background-radius: 5;"
                : "-fx-background-color: #ffffff; -fx-padding: 5; -fx-background-radius: 5;";
    }

    @Override
    public String toString() {
        return getTextoFormateado();
    }
}
