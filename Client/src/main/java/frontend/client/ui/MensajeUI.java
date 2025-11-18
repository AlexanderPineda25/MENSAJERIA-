package frontend.client.ui;

public class MensajeUI {

    public enum Tipo { TEXTO, ARCHIVO }

    private Tipo tipo;
    private String remitente;
    private String contenido;

    private String nombreArchivo;
    private long tamano;
    private Long archivoId;

    private boolean esMio;

    private byte[] contenidoArchivo; // ⭐ ARCHIVO REAL (desde historial o descarga)

    public MensajeUI(Tipo tipo, String remitente, boolean esMio) {
        this.tipo = tipo;
        this.remitente = remitente;
        this.esMio = esMio;
    }

    // =======================
    // GETTERS / SETTERS
    // =======================

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getRemitente() {
        return remitente;
    }

    public boolean esMio() {
        return esMio;
    }

    public boolean isEsMio() {
        return esMio;
    }

    // --- TEXTO ---
    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    // --- ARCHIVO ---
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
        this.tipo = Tipo.ARCHIVO; // ⭐ IMPORTANTE: asegurar tipo
    }

    public long getTamano() {
        return tamano;
    }

    public void setTamano(long tamano) {
        this.tamano = tamano;
    }

    public Long getArchivoId() {
        return archivoId;
    }

    public void setArchivoId(Long archivoId) {
        this.archivoId = archivoId;
        this.tipo = Tipo.ARCHIVO;
    }

    // --- CONTENIDO REAL DEL ARCHIVO (del historial o descarga directa) ---
    public byte[] getContenidoArchivo() {
        return contenidoArchivo;
    }

    public void setContenidoArchivo(byte[] contenidoArchivo) {
        this.contenidoArchivo = contenidoArchivo;
    }

    // =======================
    // MÉTODOS DE UTILIDAD
    // =======================

    /** Indica si ya tenemos los bytes reales del archivo */
    public boolean tieneContenido() {
        return contenidoArchivo != null && contenidoArchivo.length > 0;
    }
}
