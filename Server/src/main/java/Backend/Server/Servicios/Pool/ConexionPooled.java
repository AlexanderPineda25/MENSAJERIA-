package Backend.Server.Servicios.Pool;

import Backend.Server.Entidades.Conexion;
import lombok.Getter;

@Getter
public class ConexionPooled {

    private final Conexion conexion;
    private boolean enUso;
    private long ultimoUso;

    public ConexionPooled(Conexion conexion) {
        this.conexion = conexion;
        this.enUso = false;
        this.ultimoUso = System.currentTimeMillis();
    }

    public void marcarEnUso() {
        enUso = true;
        ultimoUso = System.currentTimeMillis();
    }

    public void liberar() {
        enUso = false;
        ultimoUso = System.currentTimeMillis();
    }

    public boolean estaExpirada(long timeoutMs) {
        return !enUso && (System.currentTimeMillis() - ultimoUso > timeoutMs);
    }
}
