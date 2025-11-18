package frontend.client.Modelos;

import java.io.Serializable;

public enum EstadoUsuario implements Serializable {

    PENDIENTE("Pendiente de aprobación"),
    ACEPTADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean puedeConectar() {
        return this == ACEPTADO;
    }

    public static EstadoUsuario fromString(String texto) {
        if (texto == null || texto.isEmpty()) {
            return PENDIENTE;
        }
        try {
            return EstadoUsuario.valueOf(texto.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDIENTE;
        }
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
