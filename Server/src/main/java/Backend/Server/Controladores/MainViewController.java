package Backend.Server.Controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class MainViewController implements Initializable {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabDashboard;
    @FXML
    private Tab tabInformes;
    @FXML
    private Tab tabUsuarios;
    @FXML
    private StackPane contentPane;
    @FXML
    private Tab tabServiciosWeb;
    @FXML
    private Tab tabLimites;

    private final ConfigurableApplicationContext context;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarContenido(tabDashboard, "/fxml/Dashboard.fxml");
        cargarContenido(tabInformes, "/fxml/Informes.fxml");
        cargarContenido(tabUsuarios, "/fxml/UsuariosPendientes.fxml");
        cargarContenido(tabServiciosWeb, "/fxml/WebServicios.fxml");
        cargarContenido(tabLimites, "/fxml/Limites.fxml");

    }

    private void cargarContenido(Tab tab, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            loader.setControllerFactory(context::getBean);
            Parent contenido = loader.load();
            tab.setContent(contenido);
        } catch (Exception e) {
            tab.setContent(new javafx.scene.control.Label("Error al cargar: " + rutaFXML));
        }
    }
}
