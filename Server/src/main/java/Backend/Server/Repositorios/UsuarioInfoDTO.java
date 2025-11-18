package Backend.Server.Repositorios;

import java.time.LocalDateTime;

public interface UsuarioInfoDTO {
    Long getId();
    String getUsername();
    String getEmail();
    String getEstado();
    Long getMensajesEnviados();
    Long getMensajesRecibidos();
    java.time.LocalDateTime getUltimaConexion();
    Boolean getConectado();
    java.time.LocalDateTime getFechaRegistro();
}