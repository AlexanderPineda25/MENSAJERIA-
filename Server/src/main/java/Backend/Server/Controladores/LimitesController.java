package Backend.Server.Controladores;

import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Servicios.Core.ConfiguracionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LimitesController {

    private final ConfiguracionService configuracionService;

    @FXML private TextField txtMaxConexionesUsuario;
    @FXML private TextField txtMaxConexionesTotales;
    @FXML private TextField txtMaxArchivosUsuario;
    @FXML private TextField txtMaxTamanoArchivo;
    @FXML private TextField txtMaxArchivosDia;
    @FXML private TextField txtMaxAudioBytes;
    @FXML private TextField txtMaxAudioSegundos;
    @FXML private TextField txtMaxMensajesMinuto;

    private ConfiguracionLimites configActual;

    @FXML
    private void initialize() {
        configActual = configuracionService.obtenerConfig();
        cargarDatos();
    }

    private void cargarDatos() {
        txtMaxConexionesUsuario.setText(String.valueOf(configActual.getMaxConexionesPorUsuario()));
        txtMaxConexionesTotales.setText(String.valueOf(configActual.getMaxConexionesTotales()));
        txtMaxArchivosUsuario.setText(String.valueOf(configActual.getMaxArchivosPorUsuario()));
        txtMaxTamanoArchivo.setText(String.valueOf(configActual.getMaxTamanoArchivoByte()));
        txtMaxArchivosDia.setText(String.valueOf(configActual.getMaxArchivosEnviadosPorDia()));
        txtMaxAudioBytes.setText(String.valueOf(configActual.getMaxTamanoAudioByte()));
        txtMaxAudioSegundos.setText(String.valueOf(configActual.getMaxDuracionAudioSegundos()));
        txtMaxMensajesMinuto.setText(String.valueOf(configActual.getMaxMensajesPorMinuto()));
    }

    @FXML
    private void guardarCambios() {
        try {
            // Usar SIEMPRE la instancia REAL cargada desde la BD
            configActual.setMaxConexionesPorUsuario(
                    Integer.parseInt(txtMaxConexionesUsuario.getText()));

            configActual.setMaxConexionesTotales(
                    Integer.parseInt(txtMaxConexionesTotales.getText()));

            configActual.setMaxArchivosPorUsuario(
                    Integer.parseInt(txtMaxArchivosUsuario.getText()));

            configActual.setMaxTamanoArchivoByte(
                    Long.parseLong(txtMaxTamanoArchivo.getText()));

            configActual.setMaxArchivosEnviadosPorDia(
                    Integer.parseInt(txtMaxArchivosDia.getText()));

            configActual.setMaxTamanoAudioByte(
                    Long.parseLong(txtMaxAudioBytes.getText()));

            configActual.setMaxDuracionAudioSegundos(
                    Integer.parseInt(txtMaxAudioSegundos.getText()));

            configActual.setMaxMensajesPorMinuto(
                    Integer.parseInt(txtMaxMensajesMinuto.getText()));

            configuracionService.guardar(configActual);

            mostrarMensaje("Cambios guardados correctamente.");

        } catch (Exception e) {
            mostrarError("Error guardando cambios: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.show();
    }
}
