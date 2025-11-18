package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.Archivo;
import Backend.Server.Entidades.Conexion;
import Backend.Server.Entidades.Mensaje;
import Backend.Server.Entidades.RegistroAccion;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Entidades.Enums.TipoAccion;
import Backend.Server.Repositorios.ArchivoRepository;
import Backend.Server.Repositorios.MensajeRepository;
import Backend.Server.Servicios.Factory.MensajeFactory;
import Backend.Server.Servicios.Observers.SistemaObserver;
import Backend.Server.Servicios.Pool.ConexionPooled;
import Backend.Server.Servicios.Pool.ConexionPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeriaService {

    private final ConexionPool conexionPool;
    private final MensajeFactory mensajeFactory;
    private final MensajeRepository mensajeRepository;
    private final ArchivoRepository archivoRepository;
    private final List<SistemaObserver> observers;

    // ============================================================
    //     PROCESAR Y GUARDAR MENSAJE
    // ============================================================
    @Transactional
    public Mensaje procesarMensaje(Usuario remitente,
                                   Usuario destinatario,
                                   String contenido,
                                   Archivo archivoAdjunto,
                                   String ipRemitente,
                                   String ipDestinatario) {

        ConexionPooled conexion = null;

        try {
            // registrar actividad
            conexion = conexionPool.adquirir(remitente, ipRemitente);
            Conexion entidad = conexion.getConexion();
            entidad.registrarMensaje();

            // ============================
            // VALIDACIÓN DE ARCHIVO (sin tocar filesystem)
            // ============================
            Archivo archivo = null;

            if (archivoAdjunto != null && archivoAdjunto.getId() != null) {
                archivo = archivoRepository.findById(archivoAdjunto.getId()).orElse(null);
            }

            // ============================
            // CREACIÓN DEL MENSAJE
            // ============================
            if (contenido == null) contenido = "";
            if (archivo != null && contenido.isEmpty()) contenido = " "; // evitar null en UI

            Mensaje mensaje = (archivo != null)
                    ? mensajeFactory.crearArchivo(remitente, destinatario, contenido, archivo, ipRemitente, ipDestinatario)
                    : mensajeFactory.crearTexto(remitente, destinatario, contenido, ipRemitente, ipDestinatario);

            // Asignar fecha manualmente (consistencia con DTO)
            mensaje.setFechaHoraEnvio(LocalDateTime.now());

            // Guardar mensaje
            mensaje = mensajeRepository.save(mensaje);

            // Recargar mensaje completamente (con archivo cargado)
            mensaje = mensajeRepository.findByIdWithArchivo(mensaje.getId());

            // Log interno
            notificarAccion(
                    TipoAccion.ENVIO_MENSAJE,
                    remitente,
                    "Mensaje enviado a " + destinatario.getUsername(),
                    ipRemitente
            );

            return mensaje;

        } finally {
            if (conexion != null) {
                conexionPool.liberar(conexion);
            }
        }
    }

    // ============================================================
    //                 HISTORIAL ENTRE USUARIOS
    // ============================================================
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerHistorial(String usuarioA, String usuarioB) {
        return mensajeRepository.findConversacionCargada(usuarioA, usuarioB);
    }

    // ============================================================
    //           HISTORIAL COMPLETO DE UN SOLO USUARIO
    // ============================================================
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerHistorialUsuario(Long usuarioId) {
        return mensajeRepository.findHistorialUsuario(usuarioId);
    }

    // ============================================================
    //                    NOTIFICACIÓN SISTEMA
    // ============================================================
    private void notificarAccion(TipoAccion tipo, Usuario usuario, String descripcion, String ip) {
        RegistroAccion accion = RegistroAccion.builder()
                .tipo(tipo)
                .usuario(usuario)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .direccionIP(ip)
                .build();

        observers.forEach(o -> o.notificar(accion));
    }
}
