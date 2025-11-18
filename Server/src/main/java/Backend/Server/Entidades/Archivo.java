package Backend.Server.Entidades;

import Backend.Server.Entidades.Enums.TipoArchivo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "archivos")
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "nombre")
    private String nombreArchivo;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "ruta")
    private String rutaAlmacenamiento;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "tamano")
    private long tamanoBytes;

    @Enumerated(EnumType.STRING)
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "tipo")
    private TipoArchivo tipoArchivo;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "mime")
    private String mimeType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "propietario_id")
    private Usuario propietario;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "duracion")
    private Integer duracionSegundos;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "formato")
    private String formatoAudio;

    @PrePersist
    private void prePersist() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
    }
}

