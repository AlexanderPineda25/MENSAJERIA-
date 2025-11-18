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
@Table(name = "configuracion_limites")
public class ConfiguracionLimites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_conexiones_usuario")
    private int maxConexionesPorUsuario;

    @Column(name = "max_conexiones_totales")
    private int maxConexionesTotales;

    @Column(name = "max_archivos_usuario")
    private int maxArchivosPorUsuario;

    @Column(name = "max_tamano_archivo")
    private long maxTamanoArchivoByte;

    @Column(name = "max_archivos_dia")
    private int maxArchivosEnviadosPorDia;

    @Column(name = "max_audio_bytes")
    private long maxTamanoAudioByte;

    @Column(name = "max_audio_segundos")
    private int maxDuracionAudioSegundos;

    @Column(name = "max_mensajes_minuto")
    private int maxMensajesPorMinuto;

    @Column(name = "activa")
    private boolean activa;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    private void prePersist() {
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
        activa = true;
    }

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
