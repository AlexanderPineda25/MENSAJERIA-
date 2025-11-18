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
@Table(name = "sesiones_activas")
public class SesionActiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "token")
    private String tokenSesion;

    @Column(name = "ip")
    private String direccionIP;

    @Column(name = "inicio")
    private LocalDateTime inicioSesion;

    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;

    @Column(name = "activa")
    private boolean activa;

    @Column(name = "aplicacion")
    private String aplicacionCliente;

    @Column(name = "version")
    private String versionCliente;

    @PrePersist
    private void prePersist() {
        if (inicioSesion == null) inicioSesion = LocalDateTime.now();
        if (ultimaActividad == null) ultimaActividad = inicioSesion;
        activa = true;
    }

    @PreUpdate
    private void preUpdate() {
        ultimaActividad = LocalDateTime.now();
    }

    public void actualizarActividad() {
        ultimaActividad = LocalDateTime.now();
    }

    public void cerrarSesion() {
        activa = false;
    }
}
