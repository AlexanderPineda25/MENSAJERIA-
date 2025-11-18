package Backend.Server.Repositorios;

import Backend.Server.Entidades.Conexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConexionRepository extends JpaRepository<Conexion, Long> {

    List<Conexion> findByUsuarioIdAndActivaTrue(Long usuarioId);

    List<Conexion> findByActivaTrue();

    List<Conexion> findByActivaFalse();

    // ========================
    // USUARIOS CONECTADOS
    // ========================
    @Query("""
        SELECT u.username AS username,
               c.direccionIP AS ip,
               c.horaConexion AS horaConexion,
               c.mensajesEnviadosEnSesion AS mensajes
        FROM Conexion c
        JOIN c.usuario u
        WHERE c.activa = true
        ORDER BY c.horaConexion DESC
    """)
    List<UsuarioConectadoDTO> findUsuariosConectados();

    // ========================
    // USUARIOS DESCONECTADOS
    // ========================
    @Query("""
        SELECT u.username AS username,
               c.direccionIP AS ip,
               c.horaConexion AS horaConexion,
               c.horaDesconexion AS horaDesconexion,
               c.mensajesEnviadosEnSesion AS mensajes
        FROM Conexion c
        JOIN c.usuario u
        WHERE c.activa = false AND c.horaDesconexion IS NOT NULL
        ORDER BY c.horaDesconexion DESC
    """)
    List<UsuarioDesconectadoDTO> findUsuariosDesconectados();


    // DTOs proyectados por JPA
    interface UsuarioConectadoDTO {
        String getUsername();
        String getIp();
        java.time.LocalDateTime getHoraConexion();
        Integer getMensajes();
    }

    interface UsuarioDesconectadoDTO {
        String getUsername();
        String getIp();
        java.time.LocalDateTime getHoraConexion();
        java.time.LocalDateTime getHoraDesconexion();
        Integer getMensajes();
    }
}

