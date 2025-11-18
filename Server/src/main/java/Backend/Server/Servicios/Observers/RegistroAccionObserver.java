package Backend.Server.Servicios.Observers;

import Backend.Server.Entidades.RegistroAccion;
import Backend.Server.Repositorios.RegistroAccionRepository;
import Backend.Server.Controladores.DashboardController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistroAccionObserver implements SistemaObserver {

    private final RegistroAccionRepository registroAccionRepository;
    private final ApplicationContext context;

    @Override
    @Async("notificacionExecutor")
    public void notificar(RegistroAccion accion) {
        registroAccionRepository.save(accion);
        System.out.println("[AUDITORÍA] " + accion.getDescripcion());

        DashboardController dashboard = context.getBean(DashboardController.class);
        dashboard.appendLog(accion.getTipo() + ": " + accion.getDescripcion());
    }
}