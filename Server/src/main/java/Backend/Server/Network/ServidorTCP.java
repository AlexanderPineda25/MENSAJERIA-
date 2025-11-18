package Backend.Server.Network;

import Backend.Server.Servicios.Core.ArchivoService;
import Backend.Server.Servicios.Core.MensajeriaService;
import Backend.Server.Servicios.Core.UsuarioService;
import Backend.Server.Servicios.Core.LoginService;
import Backend.Server.Servicios.Core.SesionActivaService;
import Backend.Server.Servicios.Core.ConexionService;
import Shared.Mensaje;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class ServidorTCP {

    private final UsuarioService usuarioService;
    private final MensajeriaService mensajeriaService;
    private final ArchivoService archivoService;
    private final LoginService loginService;
    private final SesionActivaService sesionActivaService;
    private final ConexionService conexionService;
    private final LoggerServidor loggerServidor;

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private volatile boolean activo = false;

    // ===============================
    // CONEXIONES ACTIVAS
    // ===============================
    private static final ConcurrentHashMap<String, ObjectOutputStream> conexionesActivas = new ConcurrentHashMap<>();

    // ===============================================================
    //                          INICIAR SERVIDOR
    // ===============================================================
    public void iniciar(int puerto) {
        if (activo) {
            loggerServidor.log("⚠️ El servidor ya está en ejecución.");
            return;
        }

        try {
            serverSocket = new ServerSocket(puerto);
            activo = true;

            loggerServidor.log("✅ Servidor TCP iniciado en el puerto " + puerto);

            Thread hiloAceptador = new Thread(this::esperarClientes, "Hilo-Aceptador");
            hiloAceptador.setDaemon(true);
            hiloAceptador.start();

        } catch (IOException e) {
            loggerServidor.log("❌ Error al iniciar servidor: " + e.getMessage());
        }
    }

    // ===============================================================
    //                       ESPERAR NUEVOS CLIENTES
    // ===============================================================
    private void esperarClientes() {
        while (activo) {
            try {
                Socket socketCliente = serverSocket.accept();

                loggerServidor.log(
                        "🔌 Nueva conexión desde " + socketCliente.getInetAddress().getHostAddress()
                );

                ClienteHandler handler = new ClienteHandler(
                        this,
                        socketCliente,
                        usuarioService,
                        mensajeriaService,
                        archivoService,
                        loginService,
                        sesionActivaService,
                        conexionService,
                        loggerServidor
                );

                pool.execute(handler);

            } catch (IOException e) {
                if (activo) {
                    loggerServidor.log("❌ Error aceptando cliente: " + e.getMessage());
                }
            }
        }
    }

    // ===============================================================
    //                        DETENER SERVIDOR
    // ===============================================================
    public void detener() {
        activo = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            conexionesActivas.clear();
            pool.shutdownNow();

            loggerServidor.log("🛑 Servidor detenido correctamente.");

        } catch (IOException e) {
            loggerServidor.log("❌ Error al detener servidor: " + e.getMessage());
        }
    }

    // ===============================================================
    //                   REGISTRAR / REEMPLAZAR CONEXIÓN
    // ===============================================================
    public void registrarConexion(String username, ObjectOutputStream out) {
        // Borrar conexiones antiguas
        ObjectOutputStream anterior = conexionesActivas.put(username, out);

        if (anterior != null && anterior != out) {
            try { anterior.close(); } catch (Exception ignored) {}
        }

        loggerServidor.log("🟢 Conexión registrada para usuario: " + username);
    }

    // ===============================================================
    //                     REMOVER CONEXIÓN (logout)
    // ===============================================================
    public void removerConexion(String username) {
        ObjectOutputStream out = conexionesActivas.remove(username);
        if (out != null) {
            try { out.close(); } catch (Exception ignored) {}
        }
        loggerServidor.log("🔴 Conexión eliminada para: " + username);
    }

    // ===============================================================
    //                     ENVIAR MENSAJE A USUARIO
    // ===============================================================
    public static boolean enviarAUsuario(String username, Mensaje mensaje) {
        try {
            ObjectOutputStream out = conexionesActivas.get(username);
            if (out == null) return false;

            synchronized (out) {
                out.writeObject(mensaje);
                out.flush();
                out.reset(); // ⭐ CRÍTICO: evita referencias y permite enviar archivos grandes
            }
            return true;

        } catch (Exception e) {
            // si falla, remover la conexión muerta
            conexionesActivas.remove(username);
            return false;
        }
    }

    public boolean isActivo() {
        return activo;
    }
}
