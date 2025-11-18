package Backend.Server.Entidades;

import Backend.Server.Entidades.Enums.TipoMensaje;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @Column(name = "contenido")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoMensaje tipo;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaHoraEnvio;

    @Column(name = "ip_remitente")
    private String ipRemitente;

    @Column(name = "ip_destinatario")
    private String ipDestinatario;

    @Column(name = "leido")
    private boolean leido;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "archivo_id")
    private Archivo archivo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mensaje_padre_id")
    private Mensaje mensajePadre;

    @OneToMany(mappedBy = "mensajePadre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> respuestas = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (fechaHoraEnvio == null) {
            fechaHoraEnvio = LocalDateTime.now();
        }
        if (tipo == null) {
            tipo = TipoMensaje.TEXTO;
        }
        leido = false;
    }

    public void marcarLeido() {
        leido = true;
    }

    public void adjuntarArchivo(Archivo archivo) {
        this.archivo = archivo;
        this.tipo = TipoMensaje.ARCHIVO;
    }

    public void agregarRespuesta(Mensaje respuesta) {
        if (respuesta != null && !respuestas.contains(respuesta)) {
            respuestas.add(respuesta);
            respuesta.setMensajePadre(this);
        }
    }
}
