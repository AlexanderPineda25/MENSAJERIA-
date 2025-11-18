package frontend.client.network;

import Shared.Mensaje;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ConexionCliente {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private volatile boolean conectado = false;
    private String ipServidor;
    private int puerto;
    private volatile Consumer<Mensaje> listener;

    public void conectar(String host, int puerto, Consumer<Mensaje> listenerInicial) throws IOException {
        this.ipServidor = host;
        this.puerto = puerto;
        this.listener = listenerInicial;

        socket = new Socket(host, puerto);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        conectado = true;
        System.out.println("Conectado al servidor: " + host + ":" + puerto);

        listenerThread = new Thread(this::escuchar);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void escuchar() {
        try {
            while (conectado && !socket.isClosed()) {
                Mensaje mensaje = (Mensaje) in.readObject();

                if (mensaje != null) {
                    System.out.println("📩 Recibido mensaje tipo: " + mensaje.getTipo());

                    Consumer<Mensaje> l = listener;
                    if (l != null) l.accept(mensaje);
                }
            }
        } catch (EOFException e) {
            System.out.println("Servidor cerró la conexión");
        } catch (Exception e) {
            if (conectado) System.err.println("Error en la escucha: " + e.getMessage());
        } finally {
            conectado = false;
        }
    }

    public void enviar(Mensaje mensaje) throws IOException {
        if (!conectado || socket.isClosed())
            throw new IOException("No hay conexión con el servidor");

        synchronized (out) {
            out.writeObject(mensaje);
            out.flush();
            out.reset();
        }
    }

    // -----------------------------
    // LOGIN
    // -----------------------------
    public void enviarLogin(String usuario, String password, String ipCliente) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipo("LOGIN");
        mensaje.setRemitente(usuario);
        mensaje.setContenido(password);
        mensaje.setIpRemitente(ipCliente);
        enviar(mensaje);
    }

    // -----------------------------
    // REGISTRO
    // -----------------------------
    public void enviarRegistro(String usuario, String email, String password, String ipCliente) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipo("REGISTRO");
        mensaje.setRemitente(usuario);
        mensaje.setContenido(email + "|" + password);
        mensaje.setIpRemitente(ipCliente);
        enviar(mensaje);
    }

    // -----------------------------
    // MENSAJE TEXTO
    // -----------------------------
    public void enviarMensajeTexto(String destinatario, String contenido, String ipRemitente, String remitente) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipo("MENSAJE_TEXTO");
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setContenido(contenido);
        mensaje.setIpRemitente(ipRemitente);
        enviar(mensaje);
    }

    // -----------------------------
    // MENSAJE ARCHIVO
    // -----------------------------
    public void enviarArchivo(String destinatario, File archivo, String ipRemitente, String remitente) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipo("MENSAJE_ARCHIVO");
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setNombreArchivo(archivo.getName());
        mensaje.setTamanoArchivo(archivo.length());
        mensaje.setIpRemitente(ipRemitente);

        try (FileInputStream fis = new FileInputStream(archivo)) {
            byte[] buffer = fis.readAllBytes();
            mensaje.setContenidoArchivo(buffer);
        }

        enviar(mensaje);
    }

    public void solicitarUsuariosConectados(String remitente) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipo("LISTAR_USUARIOS");
        mensaje.setRemitente(remitente);
        enviar(mensaje);
    }

    public void solicitarHistorial(String usuarioA, String usuarioB) throws IOException {
        Mensaje m = new Mensaje();
        m.setTipo("HISTORIAL");
        m.setRemitente(usuarioA);
        m.setDestinatario(usuarioB);
        enviar(m);
    }

    public void solicitarDescargaArchivo(Long archivoId) throws IOException {
        Mensaje m = new Mensaje();
        m.setTipo("DESCARGAR_ARCHIVO");
        m.setArchivoId(archivoId);
        enviar(m);
    }

    public void cerrar() {
        conectado = false;
        try {
            Mensaje despedida = new Mensaje();
            despedida.setTipo("DESCONEXION");
            enviar(despedida);
        } catch (IOException ignored) {}

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }

        if (listenerThread != null && listenerThread.isAlive())
            listenerThread.interrupt();
    }

    public boolean estaConectado() {
        return conectado && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void actualizarListener(Consumer<Mensaje> nuevoListener) {
        this.listener = nuevoListener;
    }
}
