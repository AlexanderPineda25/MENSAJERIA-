package Shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 2L;

    // ==========================================
    // IDENTIFICACIÓN GENERAL / METADATOS
    // ==========================================
    private Long idMensaje;
    private String tipo;
    private String remitente;
    private String destinatario;
    private String contenido;

    private String ipRemitente;
    private String ipDestinatario;

    private LocalDateTime fechaHora;

    // ==========================================
    // ARCHIVOS
    // ==========================================
    private String nombreArchivo;
    private long tamanoArchivo;
    private Long archivoId;
    private byte[] contenidoArchivo;

    // ==========================================
    // LISTAS / RESPUESTAS COMPLEJAS
    // ==========================================
    private String[] usuariosConectados;
    private Mensaje[] historial;

    // ==========================================
    // FLAGS DE OPERACIONES
    // ==========================================
    private boolean exito;
    private String mensajeError;

    private boolean grupal;
    private String nombreGrupo;

    private String tokenSesion;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    public Mensaje() {
        this.fechaHora = LocalDateTime.now();
    }

    public Mensaje(String tipo, String contenido) {
        this();
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public Mensaje(String tipo, String remitente, String contenido) {
        this();
        this.tipo = tipo;
        this.remitente = remitente;
        this.contenido = contenido;
    }

    // ==========================================
    // GETTERS / SETTERS
    // ==========================================
    public Long getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Long idMensaje) { this.idMensaje = idMensaje; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getIpRemitente() { return ipRemitente; }
    public void setIpRemitente(String ipRemitente) { this.ipRemitente = ipRemitente; }

    public String getIpDestinatario() { return ipDestinatario; }
    public void setIpDestinatario(String ipDestinatario) { this.ipDestinatario = ipDestinatario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }

    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public byte[] getContenidoArchivo() { return contenidoArchivo; }
    public void setContenidoArchivo(byte[] contenidoArchivo) { this.contenidoArchivo = contenidoArchivo; }

    public String[] getUsuariosConectados() { return usuariosConectados; }
    public void setUsuariosConectados(String[] usuariosConectados) { this.usuariosConectados = usuariosConectados; }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public boolean isGrupal() { return grupal; }
    public void setGrupal(boolean grupal) { this.grupal = grupal; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public String getTokenSesion() { return tokenSesion; }
    public void setTokenSesion(String tokenSesion) { this.tokenSesion = tokenSesion; }

    public Mensaje[] getHistorial() { return historial; }
    public void setHistorial(Mensaje[] historial) { this.historial = historial; }


    @Override
    public String toString() {
        return "Mensaje{" +
                "idMensaje=" + idMensaje +
                ", tipo='" + tipo + '\'' +
                ", remitente='" + remitente + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", contenido='" + contenido + '\'' +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", tamanoArchivo=" + tamanoArchivo +
                '}';
    }
}
