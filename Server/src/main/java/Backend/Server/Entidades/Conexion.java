package Backend.Server.Entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "conexiones")
public class Conexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "direccion_ip")
    private String direccionIP;

    @Column(name = "hora_conexion")
    private LocalDateTime horaConexion;

    @Column(name = "hora_desconexion")
    private LocalDateTime horaDesconexion;

    @Column(name = "mensajes_enviados")
    private long mensajesEnviadosEnSesion;

    @Column(name = "activa")
    private boolean activa;

    @Column(name = "sistema_operativo")
    private String sistemaOperativo;

    @Column(name = "dispositivo")
    private String nombreDispositivo;

    @PrePersist
    private void prePersist() {
        if (horaConexion == null) {
            horaConexion = LocalDateTime.now();
        }
        activa = true;
        if (mensajesEnviadosEnSesion < 0) {
            mensajesEnviadosEnSesion = 0;
        }
    }

    public void registrarMensaje() {
        mensajesEnviadosEnSesion++;
    }

    public void cerrarSesion() {
        activa = false;
        if (horaDesconexion == null) {
            horaDesconexion = LocalDateTime.now();
        }
    }
}
