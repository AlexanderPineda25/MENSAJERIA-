package Backend.Server.Entidades;

import Backend.Server.Entidades.Enums.TipoAccion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "registro_acciones")
public class RegistroAccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoAccion tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha")
    private LocalDateTime fechaHora;

    @Column(name = "ip")
    private String direccionIP;

    @Column(name = "detalles")
    private String detallesAdicionales;

    @PrePersist
    private void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
