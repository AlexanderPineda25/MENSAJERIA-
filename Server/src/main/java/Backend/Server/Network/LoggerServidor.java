package Backend.Server.Network;

import Backend.Server.Controladores.DashboardController;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggerServidor {

    private final String LOG_FILE = "server_log.txt";
    private DashboardController dashboard;

    public void setDashboardController(DashboardController controller) {
        this.dashboard = controller;
    }

    public synchronized void log(String mensaje) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String linea = "[" + timestamp + "] " + mensaje;

        System.out.println(linea);

        if (dashboard != null) {
            dashboard.appendLog(mensaje);
        }

        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(linea + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Error escribiendo log: " + e.getMessage());
        }
    }
}
