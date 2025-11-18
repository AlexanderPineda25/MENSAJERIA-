package Backend.Server.Repositorios;

import Backend.Server.Entidades.Usuario;
import Backend.Server.Entidades.Enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByEstado(EstadoUsuario estado);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.conexiones WHERE u.id = :id")
    Optional<Usuario> findByIdWithConexiones(@Param("id") Long id);

    @Query("SELECT u FROM Usuario u WHERE u.conectado = true")
    List<Usuario> findAllConectados();

    @Query("SELECT u FROM Usuario u WHERE u.conectado = false AND u.fechaUltimaConexion IS NOT NULL")
    List<Usuario> findAllDesconectados();

    @Query("""
    SELECT 
        u.id AS id,
        u.username AS username,
        u.email AS email,
        u.estado AS estado,
        u.conectado AS conectado,
        u.fechaRegistro AS fechaRegistro,
        u.fechaUltimaConexion AS ultimaConexion,

        COUNT(DISTINCT me.id) AS mensajesEnviados,
        COUNT(DISTINCT mr.id) AS mensajesRecibidos

    FROM Usuario u
    LEFT JOIN u.mensajesEnviados me
    LEFT JOIN u.mensajesRecibidos mr
    WHERE u.id = :id
    GROUP BY u.id, u.username, u.email, u.estado, 
             u.conectado, u.fechaRegistro, u.fechaUltimaConexion
""")
    Optional<UsuarioInfoDTO> findInfoCompletaById(@Param("id") Long id);

}
