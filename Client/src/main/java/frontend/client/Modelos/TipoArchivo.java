package frontend.client.Modelos;

import java.io.Serializable;

/**
 * Enum que representa los tipos de archivos soportados por el sistema.
 * Mantiene compatibilidad total con el servidor.
 */
public enum TipoArchivo implements Serializable {

    // Archivos de texto general
    TEXTO("Texto", new String[]{"txt"}),

    // Archivos tipo documento formal
    DOCUMENTO("Documento", new String[]{"pdf", "doc", "docx", "odt", "rtf"}),

    // Archivos binarios
    BINARIO("Binario", new String[]{"exe", "zip", "rar", "7z", "tar", "gz", "bin", "dll"}),

    // Imágenes
    IMAGEN("Imagen", new String[]{"jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico"}),

    // Audio
    AUDIO("Audio", new String[]{"mp3", "wav", "ogg", "m4a", "flac", "aac", "wma", "opus"}),

    // Video
    VIDEO("Video", new String[]{"mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v"});

    private final String descripcion;
    private final String[] extensiones;

    TipoArchivo(String descripcion, String[] extensiones) {
        this.descripcion = descripcion;
        this.extensiones = extensiones;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String[] getExtensiones() {
        return extensiones;
    }

    /** Verifica si una extensión coincide con este tipo */
    public boolean tieneExtension(String extension) {
        if (extension == null) return false;
        String ext = extension.toLowerCase().replace(".", "");

        for (String e : extensiones) {
            if (e.equals(ext)) return true;
        }
        return false;
    }

    /** Detecta el tipo de archivo según su nombre */
    public static TipoArchivo detectar(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return BINARIO;
        }

        String extension = nombreArchivo
                .substring(nombreArchivo.lastIndexOf(".") + 1)
                .toLowerCase();

        for (TipoArchivo tipo : values()) {
            if (tipo.tieneExtension(extension)) {
                return tipo;
            }
        }

        return BINARIO;
    }

    /** Obtiene el MIME type */
    public String getMimeType(String extension) {
        if (extension == null) return "application/octet-stream";
        String ext = extension.toLowerCase().replace(".", "");

        switch (this) {
            case TEXTO:
                return "text/plain";

            case DOCUMENTO:
                switch (ext) {
                    case "pdf":  return "application/pdf";
                    case "doc":  return "application/msword";
                    case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    case "odt":  return "application/vnd.oasis.opendocument.text";
                    case "rtf":  return "application/rtf";
                }
                return "application/octet-stream";

            case IMAGEN:
                return "image/" + ext;

            case AUDIO:
                return "audio/" + ext;

            case VIDEO:
                return "video/" + ext;

            case BINARIO:
            default:
                return "application/octet-stream";
        }
    }

    /** Icono representativo */
    public String getIcono() {
        switch (this) {
            case TEXTO:     return "📄";
            case DOCUMENTO: return "📝";
            case IMAGEN:    return "🖼️";
            case AUDIO:     return "🎵";
            case VIDEO:     return "🎬";
            case BINARIO:
            default:        return "📦";
        }
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
