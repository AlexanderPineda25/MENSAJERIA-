package Backend.Server.Servicios.Pool;

import Backend.Server.Entidades.Conexion;
import Backend.Server.Entidades.Usuario;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class ConexionPool {

    private final Queue<ConexionPooled> pool = new ConcurrentLinkedQueue<>();
    private static final int TAMANO_INICIAL = 10;
    private static final long TIMEOUT_EXPIRACION = 300_000;

    public ConexionPool() {
        inicializarPool();
    }

    private void inicializarPool() {
        for (int i = 0; i < TAMANO_INICIAL; i++) {

            Conexion conexion = Conexion.builder()
                    .build(); // conexión “vacía” manejada por el pool

            pool.add(new ConexionPooled(conexion));
        }

        log.info("Object Pool de conexiones inicializado con {} objetos", TAMANO_INICIAL);
    }

    public ConexionPooled adquirir(Usuario usuario, String ip) {

        ConexionPooled conexion = pool.poll();

        if (conexion == null) {

            Conexion nueva = Conexion.builder()
                    .usuario(usuario)
                    .direccionIP(ip)
                    .build();

            conexion = new ConexionPooled(nueva);
            log.info("Nueva conexión creada para usuario {}", usuario.getUsername());
        }

        conexion.marcarEnUso();
        return conexion;
    }

    public void liberar(ConexionPooled conexion) {
        if (conexion != null) {
            conexion.liberar();
            pool.offer(conexion);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void limpiarExpiradas() {
        int eliminadas = 0;

        for (ConexionPooled c : pool) {
            if (c.estaExpirada(TIMEOUT_EXPIRACION)) {
                pool.remove(c);
                eliminadas++;
            }
        }

        if (eliminadas > 0) {
            log.info("Limpieza de pool: {} conexiones expiradas eliminadas", eliminadas);
        }
    }

    @PreDestroy
    public void destruir() {
        pool.clear();
        log.info("Object Pool destruido");
    }
}
