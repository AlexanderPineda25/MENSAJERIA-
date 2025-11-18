package Backend.Server;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.ServerSocket;

@SpringBootApplication
public class ServerApplication extends Application {

    private static String[] args;
    private ConfigurableApplicationContext context;

    @Bean
    public HostServices hostServices(Application application) {
        return application.getHostServices();
    }

    @Override
    public void init() {
        try (ServerSocket socket = new ServerSocket(9090)) {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Puerto 9090 en uso.", e);
        }

        context = new SpringApplicationBuilder(ServerApplication.class)
                .headless(false)
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            ServerDesktopApp app = new ServerDesktopApp(context);
            app.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (context != null) context.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        ServerApplication.args = args;
        launch(args);
    }
}
