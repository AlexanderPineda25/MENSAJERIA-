package Backend.Server.Repositorios;

import Backend.Server.Entidades.RegistroAccion;
import Backend.Server.Entidades.Enums.TipoAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroAccionRepository extends JpaRepository<RegistroAccion, Long> {

    List<RegistroAccion> findByTipo(TipoAccion tipo);

    List<RegistroAccion> findByUsuarioId(Long usuarioId);

    List<RegistroAccion> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<RegistroAccion> findTop100ByOrderByFechaHoraDesc();
}
