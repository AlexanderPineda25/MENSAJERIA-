package Backend.Server;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class ServerDesktopApp {

    private final ConfigurableApplicationContext context;

    public ServerDesktopApp(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(context::getBean);

        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/static/css/dashboard.css").toExternalForm());

        stage.setTitle("Servidor de Mensajería - Panel de Control");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }
}
