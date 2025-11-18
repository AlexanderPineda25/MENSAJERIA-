package Backend.Server.Network;

import Backend.Server.Entidades.Archivo;
import Backend.Server.Entidades.SesionActiva;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Servicios.Core.UsuarioService;
import Backend.Server.Servicios.Core.MensajeriaService;
import Backend.Server.Servicios.Core.ArchivoService;
import Backend.Server.Servicios.Core.LoginService;
import Backend.Server.Servicios.Core.SesionActivaService;
import Backend.Server.Servicios.Core.ConexionService;
import Shared.Mensaje;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClienteHandler implements Runnable {

    private final ServidorTCP servidor;
    private final Socket socket;
    private final UsuarioService usuarioService;
    private final MensajeriaService mensajeriaService;
    private final ArchivoService archivoService;
    private final LoginService loginService;
    private final SesionActivaService sesionActivaService;
    private final ConexionService conexionService;
    private final LoggerServidor logger;

    private SesionActiva sesion;
    private Backend.Server.Entidades.Conexion conexionActual;

    public ClienteHandler(
            ServidorTCP servidor,
            Socket socket,
            UsuarioService usuarioService,
            MensajeriaService mensajeriaService,
            ArchivoService archivoService,
            LoginService loginService,
            SesionActivaService sesionActivaService,
            ConexionService conexionService,
            LoggerServidor logger
    ) {
        this.servidor = servidor;
        this.socket = socket;
        this.usuarioService = usuarioService;
        this.mensajeriaService = mensajeriaService;
        this.archivoService = archivoService;
        this.loginService = loginService;
        this.sesionActivaService = sesionActivaService;
        this.conexionService = conexionService;
        this.logger = logger;
    }

    // ============================================================
    //             BUCLE PRINCIPAL DEL CLIENTE
    // ============================================================
    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            boolean activo = true;

            while (activo && !socket.isClosed()) {
                Object obj = in.readObject();
                if (!(obj instanceof Mensaje mensaje)) continue;

                switch (mensaje.getTipo()) {
                    case "REGISTRO" -> procesarRegistro(mensaje, out);
                    case "LOGIN" -> procesarLogin(mensaje, out);
                    case "LOGOUT" -> {
                        procesarLogout(out);
                        activo = false;
                    }
                    case "LISTAR_USUARIOS" -> procesarListarUsuarios(out);
                    case "MENSAJE_TEXTO" -> procesarMensajeTexto(mensaje, out);
                    case "MENSAJE_ARCHIVO" -> procesarMensajeArchivo(mensaje, out);
                    case "HISTORIAL" -> procesarGetHistorial(mensaje, out);
                    case "DESCARGAR_ARCHIVO" -> procesarDescargarArchivo(mensaje, out);
                }
            }

        } catch (EOFException e) {
            cerrarSesionSegura();
        } catch (Exception e) {
            cerrarSesionSegura();
        }
    }

    // ============================================================
    //                         REGISTRO
    // ============================================================
    private void procesarRegistro(Mensaje mensaje, ObjectOutputStream out) throws IOException {
        String username = mensaje.getRemitente();
        String[] partes = mensaje.getContenido().split("\\|");

        if (partes.length < 2) {
            out.writeObject(new Mensaje("REGISTRO_RESPUESTA", "ERROR_FORMATO"));
            return;
        }

        try {
            usuarioService.crearUsuario(username, partes[0], partes[1]);
            out.writeObject(new Mensaje("REGISTRO_RESPUESTA", "REGISTRO_OK"));
        } catch (Exception e) {
            out.writeObject(new Mensaje("REGISTRO_RESPUESTA", "ERROR_REGISTRO"));
        }
    }

    // ============================================================
    //                          LOGIN
    // ============================================================
    private void procesarLogin(Mensaje mensaje, ObjectOutputStream out) throws IOException {
        String username = mensaje.getRemitente();
        String password = mensaje.getContenido();

        try {
            String ip = socket.getInetAddress().getHostAddress();

            SesionActiva nuevaSesion = loginService.login(
                    username, password, ip, "ClienteJava", "1.0"
            );

            this.sesion = nuevaSesion;

            // Guardamos la conexión actual del usuario
            this.conexionActual = conexionService.iniciarConexion(
                    nuevaSesion.getUsuario(),
                    ip,
                    "Desconocido",
                    "ClienteJava"
            );

            servidor.registrarConexion(username, out);

            Mensaje respuesta = new Mensaje("LOGIN_RESPUESTA", "LOGIN_OK");
            respuesta.setTokenSesion(nuevaSesion.getTokenSesion());

            out.writeObject(respuesta);

        } catch (IllegalArgumentException e) {
            out.writeObject(new Mensaje("LOGIN_RESPUESTA", e.getMessage()));
        } catch (Exception e) {
            out.writeObject(new Mensaje("LOGIN_RESPUESTA", "ERROR_LOGIN"));
        }
    }

    // ============================================================
    //                          LOGOUT
    // ============================================================
    private void procesarLogout(ObjectOutputStream out) throws IOException {
        cerrarSesionSegura();
        out.writeObject(new Mensaje("LOGOUT_RESPUESTA", "OK"));
    }

    /**
     * Cierra todo correctamente:
     * - Sesión activa
     * - Conexión en la tabla "conexiones"
     * - Remueve del mapa de conexiones activas
     * - Cierra el socket
     */
    private void cerrarSesionSegura() {
        try {
            if (sesion != null) {
                sesionActivaService.cerrarSesion(sesion.getTokenSesion());
            }

            if (conexionActual != null) {
                conexionService.cerrarConexion(conexionActual);
            }

            if (sesion != null && sesion.getUsuario() != null) {
                servidor.removerConexion(sesion.getUsuario().getUsername());
            }

            try { socket.close(); } catch (Exception ignored) {}

        } catch (Exception ignored) {}
    }

    // ============================================================
    //                    LISTAR USUARIOS
    // ============================================================
    private void procesarListarUsuarios(ObjectOutputStream out) throws IOException {
        try {
            var usuarios = usuarioService.listarAceptados();

            String[] nombres = usuarios.stream()
                    .map(Usuario::getUsername)
                    .toArray(String[]::new);

            Mensaje respuesta = new Mensaje("LISTAR_USUARIOS_RESPUESTA", "");
            respuesta.setUsuariosConectados(nombres);
            out.writeObject(respuesta);

        } catch (Exception e) {
            out.writeObject(new Mensaje("LISTAR_USUARIOS_RESPUESTA", "ERROR_LISTAR"));
        }
    }

    // ============================================================
    //                     MENSAJE DE TEXTO
    // ============================================================
    private void procesarMensajeTexto(Mensaje mensaje, ObjectOutputStream out) throws IOException {
        try {
            Usuario remitente = usuarioService.buscarPorUsername(mensaje.getRemitente());
            Usuario destinatario = usuarioService.buscarPorUsername(mensaje.getDestinatario());

            var mensajeDB = mensajeriaService.procesarMensaje(
                    remitente,
                    destinatario,
                    mensaje.getContenido() == null ? "" : mensaje.getContenido(),
                    null,
                    socket.getInetAddress().getHostAddress(),
                    null
            );

            Mensaje notificacion = new Mensaje();
            notificacion.setTipo("MENSAJE_TEXTO");
            notificacion.setRemitente(remitente.getUsername());
            notificacion.setDestinatario(destinatario.getUsername());
            notificacion.setContenido(mensaje.getContenido());
            notificacion.setIdMensaje(mensajeDB.getId());
            notificacion.setFechaHora(mensajeDB.getFechaHoraEnvio());

            ServidorTCP.enviarAUsuario(destinatario.getUsername(), notificacion);

            out.writeObject(new Mensaje("MENSAJE_TEXTO_RESPUESTA", "OK"));

        } catch (Exception e) {
            out.writeObject(new Mensaje("MENSAJE_TEXTO_RESPUESTA", "ERROR"));
        }
    }

    // ============================================================
    //                     MENSAJE CON ARCHIVO
    // ============================================================
    private void procesarMensajeArchivo(Mensaje mensaje, ObjectOutputStream out) throws IOException {
        try {
            Usuario remitente = usuarioService.buscarPorUsername(mensaje.getRemitente());
            Usuario destinatario = usuarioService.buscarPorUsername(mensaje.getDestinatario());

            byte[] datos = mensaje.getContenidoArchivo();
            if (datos == null) {
                out.writeObject(new Mensaje("MENSAJE_ARCHIVO_RESPUESTA", "SIN_DATOS"));
                return;
            }

            Path base = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(base);

            String nombreFisico = System.currentTimeMillis() + "_" + mensaje.getNombreArchivo();
            Path ruta = base.resolve(nombreFisico).toAbsolutePath().normalize();
            Files.write(ruta, datos);

            long tamano = mensaje.getTamanoArchivo() > 0 ? mensaje.getTamanoArchivo() : datos.length;

            Archivo archivo = archivoService.subirArchivo(
                    remitente,
                    mensaje.getNombreArchivo(),
                    ruta.toString(),
                    tamano,
                    Backend.Server.Entidades.Enums.TipoArchivo.DOCUMENTO,
                    "application/octet-stream",
                    null,
                    null
            );

            var mensajeDB = mensajeriaService.procesarMensaje(
                    remitente,
                    destinatario,
                    mensaje.getContenido() != null ? mensaje.getContenido() : "",
                    archivo,
                    socket.getInetAddress().getHostAddress(),
                    null
            );

            Mensaje notificacion = new Mensaje();
            notificacion.setTipo("MENSAJE_ARCHIVO");
            notificacion.setRemitente(remitente.getUsername());
            notificacion.setDestinatario(destinatario.getUsername());
            notificacion.setArchivoId(archivo.getId());
            notificacion.setNombreArchivo(archivo.getNombreArchivo());
            notificacion.setTamanoArchivo(archivo.getTamanoBytes());
            notificacion.setIdMensaje(mensajeDB.getId());
            notificacion.setFechaHora(mensajeDB.getFechaHoraEnvio());

            ServidorTCP.enviarAUsuario(destinatario.getUsername(), notificacion);

            out.writeObject(new Mensaje("MENSAJE_ARCHIVO_RESPUESTA", "OK"));

        } catch (Exception e) {
            out.writeObject(new Mensaje("MENSAJE_ARCHIVO_RESPUESTA", "ERROR"));
        }
    }

    // ============================================================
    //                        HISTORIAL
    // ============================================================
    private void procesarGetHistorial(Mensaje mensaje, ObjectOutputStream out) throws IOException {

        var lista = mensajeriaService.obtenerHistorial(
                mensaje.getRemitente(), mensaje.getDestinatario()
        );

        Mensaje respuesta = new Mensaje("HISTORIAL_RESPUESTA", "");

        Mensaje[] historialDTO = lista.stream().map(m -> {
            Mensaje dto = new Mensaje();
            dto.setTipo(m.getArchivo() == null ? "MENSAJE_TEXTO" : "MENSAJE_ARCHIVO");
            dto.setRemitente(m.getRemitente().getUsername());
            dto.setDestinatario(m.getDestinatario().getUsername());
            dto.setContenido(m.getContenido());
            dto.setFechaHora(m.getFechaHoraEnvio());
            dto.setIdMensaje(m.getId());

            if (m.getArchivo() != null) {
                try {
                    Archivo a = m.getArchivo();
                    dto.setArchivoId(a.getId());
                    dto.setNombreArchivo(a.getNombreArchivo());
                    dto.setTamanoArchivo(a.getTamanoBytes());

                    byte[] bytes = Files.readAllBytes(Paths.get(a.getRutaAlmacenamiento()));
                    dto.setContenidoArchivo(bytes);

                } catch (Exception ex) {
                    System.out.println("Error leyendo archivo historial: " + ex.getMessage());
                }
            }

            return dto;
        }).toArray(Mensaje[]::new);

        respuesta.setHistorial(historialDTO);
        out.writeObject(respuesta);
    }

    // ============================================================
    //                    DESCARGAR ARCHIVO
    // ============================================================
    private void procesarDescargarArchivo(Mensaje mensaje, ObjectOutputStream out) throws IOException {
        try {
            if (mensaje.getArchivoId() == null) {
                out.writeObject(new Mensaje("DESCARGAR_ARCHIVO_RESPUESTA", "ERROR"));
                return;
            }

            Archivo archivo = archivoService.obtenerPorId(mensaje.getArchivoId());

            byte[] bytes = Files.readAllBytes(Paths.get(archivo.getRutaAlmacenamiento()));

            Mensaje respuesta = new Mensaje();
            respuesta.setTipo("DESCARGAR_ARCHIVO_RESPUESTA");
            respuesta.setNombreArchivo(archivo.getNombreArchivo());
            respuesta.setTamanoArchivo(archivo.getTamanoBytes());
            respuesta.setContenidoArchivo(bytes);

            out.writeObject(respuesta);

        } catch (Exception e) {
            out.writeObject(new Mensaje("DESCARGAR_ARCHIVO_RESPUESTA", "ERROR"));
        }
    }
}
