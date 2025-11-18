package Backend.Server.Controladores;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebServiciosController {

    private final HostServices hostServices;

    private static final String BASE_URL = "http://localhost:8080";

    private void abrirURL(String ruta) {
        try {
            hostServices.showDocument(BASE_URL + ruta);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No se pudo abrir el servicio web");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // =============================
    //           INFORMES
    // =============================
    @FXML
    public void abrirInformes() {
        abrirURL("/informes");
    }

    // =============================
    //           REST API
    // =============================

    // Mostrar info completa de un usuario
    @FXML
    public void abrirApiUsuarios() {
        abrirURL("/api/web");
    }

}
