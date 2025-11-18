package Backend.Server.Utils.Dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UsuarioListadoDTO {

    private Long id;
    private String username;
    private String email;
    private Boolean activo;
    private Boolean conectado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaConexion;

    public UsuarioListadoDTO(Long id, String username, String email,
                             Boolean activo, Boolean conectado,
                             LocalDateTime fechaRegistro,
                             LocalDateTime ultimaConexion) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.activo = activo;
        this.conectado = conectado;
        this.fechaRegistro = fechaRegistro;
        this.ultimaConexion = ultimaConexion;
    }
}
