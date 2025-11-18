package frontend.client.Modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String password;
    private EstadoUsuario estado;
    private boolean conectado;
    private LocalDateTime fechaUltimaConexion;
    private LocalDateTime fechaRegistro;

    private String direccionIP;

    // 🔥 CAMPOS QUE FALTABAN
    private int mensajesEnviados;
    private int mensajesRecibidos;

    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = EstadoUsuario.PENDIENTE;
        this.conectado = false;
    }

    public Usuario(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Usuario(String username) {
        this();
        this.username = username;
    }

    // ==========================
    //       GETTERS / SETTERS
    // ==========================

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public EstadoUsuario getEstado() { return estado; }

    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public boolean isConectado() { return conectado; }

    public void setConectado(boolean conectado) { this.conectado = conectado; }

    public LocalDateTime getFechaUltimaConexion() { return fechaUltimaConexion; }

    public void setFechaUltimaConexion(LocalDateTime fechaUltimaConexion) {
        this.fechaUltimaConexion = fechaUltimaConexion;
    }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDireccionIP() { return direccionIP; }

    public void setDireccionIP(String direccionIP) { this.direccionIP = direccionIP; }

    // 🔥 NUEVOS GETTERS/SETTERS
    public int getMensajesEnviados() { return mensajesEnviados; }

    public void setMensajesEnviados(int mensajesEnviados) { this.mensajesEnviados = mensajesEnviados; }

    public int getMensajesRecibidos() { return mensajesRecibidos; }

    public void setMensajesRecibidos(int mensajesRecibidos) { this.mensajesRecibidos = mensajesRecibidos; }

    // ==========================
    //       AUXILIARES
    // ==========================

    public void incrementarMensajesEnviados() {
        this.mensajesEnviados++;
    }

    public void incrementarMensajesRecibidos() {
        this.mensajesRecibidos++;
    }

    public String getEstadoTexto() {
        return conectado ? "Conectado" : "Desconectado";
    }

    public String getEstadoCuenta() {
        if (estado == null) return "Desconocido";

        switch (estado) {
            case PENDIENTE: return "Pendiente de aprobación";
            case ACEPTADO: return "Activo";
            case RECHAZADO: return "Rechazado";
            default: return "Desconocido";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(username, usuario.username);
    }

    @Override
    public int hashCode() { return Objects.hash(username); }

    @Override
    public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", estado=" + estado +
                ", conectado=" + conectado +
                ", ip='" + direccionIP + '\'' +
                '}';
    }
}
