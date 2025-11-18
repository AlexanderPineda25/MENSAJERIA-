package Backend.Server.Repositorios;

import Backend.Server.Entidades.SesionActiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionActivaRepository extends JpaRepository<SesionActiva, Long> {

    Optional<SesionActiva> findByTokenSesion(String token);

    List<SesionActiva> findByUsuarioIdAndActivaTrue(Long usuarioId);

    List<SesionActiva> findByActivaTrue();

    @Query("SELECT s FROM SesionActiva s WHERE s.ultimaActividad < :timeout AND s.activa = true")
    List<SesionActiva> findSesionesExpiradas(@Param("timeout") LocalDateTime timeout);

    void deleteByTokenSesion(String token);
}
