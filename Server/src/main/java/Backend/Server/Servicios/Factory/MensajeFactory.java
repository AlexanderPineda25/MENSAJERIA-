package Backend.Server.Servicios.Factory;

import Backend.Server.Entidades.Archivo;
import Backend.Server.Entidades.Enums.TipoMensaje;
import Backend.Server.Entidades.Mensaje;
import Backend.Server.Entidades.Usuario;
import org.springframework.stereotype.Component;

@Component
public class MensajeFactory {

    public Mensaje crearTexto(Usuario remitente,
                              Usuario destinatario,
                              String contenido,
                              String ipRemitente,
                              String ipDestinatario) {

        return Mensaje.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .contenido(contenido)
                .tipo(TipoMensaje.TEXTO)
                .ipRemitente(ipRemitente)
                .ipDestinatario(ipDestinatario)
                .build();
    }

    public Mensaje crearArchivo(Usuario remitente,
                                Usuario destinatario,
                                String contenido,
                                Archivo archivo,
                                String ipRemitente,
                                String ipDestinatario) {

        return Mensaje.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .contenido(contenido)
                .tipo(TipoMensaje.ARCHIVO)
                .archivo(archivo)
                .ipRemitente(ipRemitente)
                .ipDestinatario(ipDestinatario)
                .build();
    }

    public Mensaje crearRespuesta(Mensaje padre,
                                  Usuario remitente,
                                  Usuario destinatario,
                                  String contenido) {

        Mensaje respuesta = Mensaje.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .contenido(contenido)
                .tipo(TipoMensaje.TEXTO)
                .mensajePadre(padre)
                .build();

        padre.agregarRespuesta(respuesta);
        return respuesta;
    }
}
