package frontend.client.Modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Archivo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nombreArchivo;
    private long tamanoBytes;
    private TipoArchivo tipoArchivo;
    private String mimeType;

    // El servidor envía propietario como USERNAME (lo dejamos así)
    private String propietario;

    private LocalDateTime fechaSubida;

    // El cliente usa estos campos para manejo local
    private byte[] contenido;
    private boolean descargado;
    private LocalDateTime fechaDescarga;

    // Datos opcionales de audio
    private Integer duracionSegundos;
    private String formatoAudio;

    public Archivo() {
        this.fechaSubida = LocalDateTime.now();
        this.descargado = false;
    }

    public Archivo(String nombreArchivo, long tamanoBytes, TipoArchivo tipoArchivo) {
        this();
        this.nombreArchivo = nombreArchivo;
        this.tamanoBytes = tamanoBytes;
        this.tipoArchivo = tipoArchivo;
    }

    public Archivo(String nombreArchivo, byte[] contenido, String propietario) {
        this();
        this.nombreArchivo = nombreArchivo;
        this.contenido = contenido;
        this.tamanoBytes = contenido != null ? contenido.length : 0;
        this.propietario = propietario;
        this.tipoArchivo = detectarTipoArchivo(nombreArchivo);
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public TipoArchivo getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(TipoArchivo tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
        if (contenido != null) this.tamanoBytes = contenido.length;
    }

    public boolean isDescargado() { return descargado; }
    public void setDescargado(boolean descargado) { this.descargado = descargado; }

    public LocalDateTime getFechaDescarga() { return fechaDescarga; }
    public void setFechaDescarga(LocalDateTime fechaDescarga) { this.fechaDescarga = fechaDescarga; }

    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    public String getFormatoAudio() { return formatoAudio; }
    public void setFormatoAudio(String formatoAudio) { this.formatoAudio = formatoAudio; }

    // UTILIDADES
    public String getExtension() {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return "";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    private TipoArchivo detectarTipoArchivo(String nombre) {
        if (nombre == null) return TipoArchivo.BINARIO;
        String ext = getExtension();

        if (ext.matches("txt|doc|docx|pdf|odt|rtf")) return TipoArchivo.TEXTO;
        if (ext.matches("jpg|jpeg|png|gif|bmp|svg|webp")) return TipoArchivo.IMAGEN;
        if (ext.matches("mp3|wav|ogg|m4a|flac|aac|wma")) return TipoArchivo.AUDIO;
        if (ext.matches("mp4|avi|mkv|mov|wmv|flv|webm")) return TipoArchivo.VIDEO;

        return TipoArchivo.BINARIO;
    }

    public String getTamanoFormateado() {
        if (tamanoBytes < 1024) return tamanoBytes + " B";
        if (tamanoBytes < 1024 * 1024) return String.format("%.2f KB", tamanoBytes / 1024.0);
        if (tamanoBytes < 1024L * 1024 * 1024) return String.format("%.2f MB", tamanoBytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", tamanoBytes / (1024.0 * 1024.0 * 1024.0));
    }

    public String getFechaFormateada() {
        if (fechaSubida == null) return "";
        return fechaSubida.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public boolean esImagen() { return tipoArchivo == TipoArchivo.IMAGEN; }
    public boolean esAudio()  { return tipoArchivo == TipoArchivo.AUDIO; }
    public boolean esVideo()  { return tipoArchivo == TipoArchivo.VIDEO; }
    public boolean esTexto()  { return tipoArchivo == TipoArchivo.TEXTO; }

    public String getIcono() {
        if (tipoArchivo == null) return "📦";
        switch (tipoArchivo) {
            case TEXTO: return "📄";
            case IMAGEN: return "🖼️";
            case AUDIO: return "🎵";
            case VIDEO: return "🎬";
            default: return "📦";
        }
    }

    public void marcarComoDescargado() {
        this.descargado = true;
        this.fechaDescarga = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Archivo)) return false;
        Archivo archivo = (Archivo) o;
        return Objects.equals(id, archivo.id) &&
                Objects.equals(nombreArchivo, archivo.nombreArchivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreArchivo);
    }

    @Override
    public String toString() {
        return "Archivo{" +
                "nombre='" + nombreArchivo + '\'' +
                ", tamaño=" + getTamanoFormateado() +
                ", tipo=" + tipoArchivo +
                ", propietario='" + propietario + '\'' +
                ", fecha=" + getFechaFormateada() +
                '}';
    }
}
