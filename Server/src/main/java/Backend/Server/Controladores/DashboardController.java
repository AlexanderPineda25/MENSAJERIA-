package Backend.Server.Controladores;

import Backend.Server.Network.LoggerServidor;
import Backend.Server.Network.ServidorTCP;
import Backend.Server.Servicios.Core.ConexionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.lang.management.ManagementFactory;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DashboardController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private Label portLabel;
    @FXML private Label usersLabel;
    @FXML private Label cpuLabel;
    @FXML private Label ramLabel;
    @FXML private TextArea logArea;
    @FXML private TextField portField;

    private final ServidorTCP servidorTCP;
    private final ConexionService conexionService;
    private final LoggerServidor loggerServidor;
    private final ConfigurableApplicationContext context;

    private final ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setText("Servidor detenido");
        statusLabel.setStyle("-fx-text-fill: red;");
        portLabel.setText("Puerto actual: -");
        appendLog("Panel iniciado. Ingrese un puerto y presione 'Iniciar Servidor'.");
        startMonitoring();
        loggerServidor.setDashboardController(this);
    }

    private void startMonitoring() {
        monitorExecutor.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            var osBean = (com.sun.management.OperatingSystemMXBean)
                    ManagementFactory.getOperatingSystemMXBean();

            double cpuLoad = osBean.getSystemLoadAverage();
            if (cpuLoad < 0) cpuLoad = 0;
            if (cpuLoad > 1) cpuLoad = 1;

            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            double ramPercent = (total - free) * 100.0 / total;

            cpuLabel.setText(String.format("CPU: %.1f%%", cpuLoad * 100));
            ramLabel.setText(String.format("RAM: %.1f%%", ramPercent));

            long count = conexionService.contarConexionesActivas();
            usersLabel.setText("Usuarios conectados: " + count);

        }), 0, 2, TimeUnit.SECONDS);
    }

    @FXML
    private void onStartServer() {
        if (servidorTCP.isActivo()) {
            appendLog("⚠️ El servidor ya está ejecutándose.");
            return;
        }

        int puerto;
        try {
            puerto = Integer.parseInt(portField.getText().trim());
        } catch (Exception e) {
            appendLog("❌ Puerto inválido.");
            return;
        }

        servidorTCP.iniciar(puerto);

        statusLabel.setText("Servidor activo");
        statusLabel.setStyle("-fx-text-fill: green;");
        portLabel.setText("Puerto actual: " + puerto);

        appendLog("Servidor TCP iniciado en puerto " + puerto);
    }

    @FXML
    private void onRestart() {
        appendLog("🔄 Reiniciando servidor...");
        servidorTCP.detener();

        statusLabel.setText("Reiniciando...");
        statusLabel.setStyle("-fx-text-fill: orange;");

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            Platform.runLater(this::onStartServer);
        }).start();
    }

    @FXML
    private void onShutdown() {
        appendLog("⏻ Apagando servidor...");
        servidorTCP.detener();
        monitorExecutor.shutdownNow();

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            context.close();
            Platform.exit();
            Runtime.getRuntime().halt(0);
        }).start();
    }

    public void appendLog(String message) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() ->
                logArea.appendText("[" + timestamp + "] " + message + "\n"));
    }
}
