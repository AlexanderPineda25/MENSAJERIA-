package Backend.Server.Servicios.Observers;

import Backend.Server.Entidades.RegistroAccion;

public interface SistemaObserver {
    void notificar(RegistroAccion accion);
}
