package Backend.Server.Repositorios;

import Backend.Server.Entidades.Mensaje;
import Backend.Server.Entidades.Enums.TipoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByRemitenteId(Long remitenteId);

    List<Mensaje> findByDestinatarioId(Long destinatarioId);

    List<Mensaje> findByTipo(TipoMensaje tipo);

    @Query("""
            SELECT m FROM Mensaje m 
            LEFT JOIN FETCH m.archivo 
            WHERE m.id = :id
            """)
    Mensaje findByIdWithArchivo(@Param("id") Long id);

    @Query("""
    SELECT 
        m.ipRemitente AS ipOrigen,
        m.ipDestinatario AS ipDestino,
        m.fechaHoraEnvio AS fechaHora,
        m.destinatario.username AS remoto,
        m.contenido AS contenido
    FROM Mensaje m
    WHERE m.remitente.id = :usuarioId
    ORDER BY m.fechaHoraEnvio DESC
""")
    List<MensajeEnviadoDTO> findMensajesEnviadosByUsuarioId(@Param("usuarioId") Long usuarioId);


    @Query("""
    SELECT 
        m.ipRemitente AS ipOrigen,
        m.ipDestinatario AS ipDestino,
        m.fechaHoraEnvio AS fechaHora,
        m.remitente.username AS remoto,
        m.contenido AS contenido
    FROM Mensaje m
    WHERE m.destinatario.id = :usuarioId
    ORDER BY m.fechaHoraEnvio DESC
""")
    List<MensajeRecibidoDTO> findMensajesRecibidosByUsuarioId(@Param("usuarioId") Long usuarioId);



    @Query("""
            SELECT DISTINCT m FROM Mensaje m
            JOIN FETCH m.remitente r
            JOIN FETCH m.destinatario d
            LEFT JOIN FETCH m.archivo a
            WHERE (r.username = :a AND d.username = :b)
               OR (r.username = :b AND d.username = :a)
            ORDER BY m.fechaHoraEnvio ASC
            """)
    List<Mensaje> findConversacionCargada(@Param("a") String a, @Param("b") String b);


    @Query("""
            SELECT DISTINCT m FROM Mensaje m
            LEFT JOIN FETCH m.archivo a
            LEFT JOIN FETCH m.remitente r
            LEFT JOIN FETCH m.destinatario d
            WHERE m.remitente.id = :usuarioId
               OR m.destinatario.id = :usuarioId
            ORDER BY m.fechaHoraEnvio ASC
            """)
    List<Mensaje> findHistorialUsuario(@Param("usuarioId") Long usuarioId);


    @Query("""
            SELECT m.remitente.username AS username, COUNT(m.id) AS total
            FROM Mensaje m
            GROUP BY m.remitente.username
            ORDER BY total DESC
            """)
    List<Object[]> obtenerRankingUsuarios();

    default Object[] obtenerUsuarioMasActivo() {
        var lista = obtenerRankingUsuarios();
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Query("""
       SELECT DISTINCT m FROM Mensaje m
       LEFT JOIN FETCH m.remitente
       LEFT JOIN FETCH m.destinatario
       ORDER BY m.fechaHoraEnvio DESC
       """)
    List<Mensaje> findAllWithUsuarios();


    interface MensajeEnviadoDTO {
        String getIpOrigen();
        String getIpDestino();
        LocalDateTime getFechaHora();
        String getRemoto();
        String getContenido();
    }


    interface MensajeRecibidoDTO {
        String getIpOrigen();
        String getIpDestino();
        LocalDateTime getFechaHora();
        String getRemoto();
        String getContenido();
    }
}
