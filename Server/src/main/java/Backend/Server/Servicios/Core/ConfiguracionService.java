package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Repositorios.ConfiguracionLimitesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionLimitesRepository repo;

    public ConfiguracionLimites obtenerConfig() {
        return repo.findByActivaTrue()
                .orElseThrow(() -> new RuntimeException("No existe una configuración activa."));
    }

    @Transactional
    public ConfiguracionLimites guardar(ConfiguracionLimites config) {
        return repo.save(config);
    }
}
