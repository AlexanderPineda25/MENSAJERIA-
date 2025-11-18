package Backend.Server.Repositorios;

import Backend.Server.Entidades.Archivo;
import Backend.Server.Entidades.Enums.TipoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Long> {

    List<Archivo> findByPropietarioId(Long propietarioId);

    List<Archivo> findByTipoArchivo(TipoArchivo tipo);

    @Query("""
        SELECT a.id AS id,
               a.nombreArchivo AS nombre, 
               a.tamanoBytes AS tamano, 
               a.fechaSubida AS fecha,
               u.username AS propietario
        FROM Archivo a
        JOIN a.propietario u
        ORDER BY a.tamanoBytes DESC
        """)
    List<ArchivoInformeDTO> findAllOrdenadosPorTamano();


    interface ArchivoInformeDTO {
        Long getId();
        String getNombre();
        Long getTamano();
        java.time.LocalDateTime getFecha();
        String getPropietario();
    }

}