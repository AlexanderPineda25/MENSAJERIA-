package Backend.Server.Repositorios;

import Backend.Server.Entidades.ConfiguracionLimites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionLimitesRepository extends JpaRepository<ConfiguracionLimites, Long> {

    Optional<ConfiguracionLimites> findByActivaTrue();
}
