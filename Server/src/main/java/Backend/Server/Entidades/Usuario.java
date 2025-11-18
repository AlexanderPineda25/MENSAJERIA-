package Backend.Server.Entidades;

import Backend.Server.Entidades.Enums.EstadoUsuario;
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
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado;

    private boolean conectado;

    @Column(name = "fecha_ultima_conexion")
    private LocalDateTime fechaUltimaConexion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    private boolean activo;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Conexion> conexiones = new ArrayList<>();

    @OneToMany(mappedBy = "remitente", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesEnviados = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", fetch = FetchType.LAZY)
    private List<Mensaje> mensajesRecibidos = new ArrayList<>();

    @OneToMany(mappedBy = "propietario", fetch = FetchType.LAZY)
    private List<Archivo> archivos = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = EstadoUsuario.PENDIENTE;
        activo = true;
        conectado = false;
    }
}

