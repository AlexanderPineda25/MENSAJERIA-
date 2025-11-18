package Backend.Server.Controladores;

import Backend.Server.Entidades.Usuario;
import Backend.Server.Servicios.Core.UsuarioService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UsuariosPendientesController {

    @FXML private TableView<Usuario> tablaPendientes;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, Void> colAccion;

    private final UsuarioService usuarioService;
    private final DashboardController dashboard;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    private void initialize() {
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        colAccion.setCellFactory(param -> new TableCell<>() {

            private final Button btnAceptar = new Button("Aceptar");
            private final Button btnRechazar = new Button("Rechazar");
            private final HBox contenedor = new HBox(5, btnAceptar, btnRechazar);

            {
                btnAceptar.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
                btnRechazar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Usuario u = getTableView().getItems().get(getIndex());

                btnAceptar.setOnAction(e -> aceptar(u));
                btnRechazar.setOnAction(e -> rechazar(u));

                setGraphic(contenedor);
            }
        });

        cargarPendientes();
        programarActualizacionAutomatica();
    }

    private void aceptar(Usuario u) {
        usuarioService.aceptarUsuario(u.getId());
        dashboard.appendLog("Usuario aceptado: " + u.getUsername());
        cargarPendientes();
    }

    private void rechazar(Usuario u) {
        usuarioService.rechazarUsuario(u.getId());
        dashboard.appendLog("Usuario rechazado: " + u.getUsername());
        cargarPendientes();
    }

    private void cargarPendientes() {
        Platform.runLater(() -> {
            tablaPendientes.setItems(
                    FXCollections.observableArrayList(usuarioService.listarPendientes())
            );
        });
    }

    private void programarActualizacionAutomatica() {
        scheduler.scheduleAtFixedRate(this::cargarPendientes, 0, 5, TimeUnit.SECONDS);
    }
}
