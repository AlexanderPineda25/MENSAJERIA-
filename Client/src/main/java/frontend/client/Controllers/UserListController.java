package frontend.client.Controllers;

import frontend.client.Modelos.Usuario;
import frontend.client.Modelos.EstadoUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserListController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, String> colIP;
    @FXML private TableColumn<Usuario, String> colConexion;
    @FXML private TableColumn<Usuario, Integer> colMensajes;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltro;
    @FXML private Label lblTotal;
    @FXML private Label lblConectados;
    @FXML private Label lblDesconectados;
    @FXML private Button btnRefrescar;
    @FXML private Button btnVerDetalles;

    private ObservableList<Usuario> listaUsuarios;
    private ObservableList<Usuario> listaFiltrada;
    private DateTimeFormatter dateFormatter;

    @FXML
    public void initialize() {

        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        listaUsuarios = FXCollections.observableArrayList();
        listaFiltrada = FXCollections.observableArrayList();

        configurarTabla();
        configurarFiltros();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarUsuarios());
        }

        if (tableUsuarios != null) {
            tableUsuarios.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> actualizarBotonesSeleccion()
            );
        }
    }

    private void configurarTabla() {
        if (tableUsuarios == null) return;

        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        colEstado.setCellValueFactory(cell -> {
            Usuario u = cell.getValue();
            String estado = u.isConectado() ? "🟢 Conectado" : "⚫ Desconectado";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });

        colIP.setCellValueFactory(cell -> {
            String ip = cell.getValue().getDireccionIP();
            return new javafx.beans.property.SimpleStringProperty(ip != null ? ip : "N/A");
        });

        colConexion.setCellValueFactory(cell -> {
            if (cell.getValue().getFechaUltimaConexion() != null)
                return new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getFechaUltimaConexion().format(dateFormatter));
            return new javafx.beans.property.SimpleStringProperty("Nunca");
        });

        colMensajes.setCellValueFactory(cell -> {
            Usuario u = cell.getValue();
            int total = u.getMensajesEnviados() + u.getMensajesRecibidos();
            return new javafx.beans.property.SimpleObjectProperty<>(total);
        });

        tableUsuarios.setItems(listaFiltrada);
        tableUsuarios.setPlaceholder(new Label("No hay usuarios para mostrar"));
    }

    private void configurarFiltros() {
        cmbFiltro.setItems(FXCollections.observableArrayList(
                "Todos",
                "Conectados",
                "Desconectados",
                "Aceptados",
                "Pendientes",
                "Rechazados"
        ));
        cmbFiltro.setValue("Todos");

        cmbFiltro.valueProperty().addListener((obs, oldVal, newVal) -> filtrarUsuarios());
    }

    public void setUsuarios(List<Usuario> usuarios) {
        Platform.runLater(() -> {
            listaUsuarios.clear();
            if (usuarios != null) listaUsuarios.addAll(usuarios);
            filtrarUsuarios();
            actualizarEstadisticas();
        });
    }

    public void agregarUsuario(Usuario usuario) {
        Platform.runLater(() -> {
            listaUsuarios.add(usuario);
            filtrarUsuarios();
            actualizarEstadisticas();
        });
    }

    public void actualizarUsuario(Usuario usuario) {
        Platform.runLater(() -> {
            int idx = listaUsuarios.indexOf(usuario);
            if (idx >= 0) {
                listaUsuarios.set(idx, usuario);
                filtrarUsuarios();
                actualizarEstadisticas();
            }
        });
    }

    public void eliminarUsuario(Usuario usuario) {
        Platform.runLater(() -> {
            listaUsuarios.remove(usuario);
            filtrarUsuarios();
            actualizarEstadisticas();
        });
    }

    @FXML
    private void filtrarUsuarios() {
        listaFiltrada.clear();

        String texto = txtBuscar.getText().toLowerCase();
        String filtro = cmbFiltro.getValue();

        for (Usuario u : listaUsuarios) {

            boolean matchTexto =
                    texto.isEmpty() ||
                            u.getUsername().toLowerCase().contains(texto) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(texto)) ||
                            (u.getDireccionIP() != null && u.getDireccionIP().contains(texto));

            if (!matchTexto) continue;

            boolean matchFiltro = switch (filtro) {
                case "Conectados" -> u.isConectado();
                case "Desconectados" -> !u.isConectado();
                case "Aceptados" -> u.getEstado() == EstadoUsuario.ACEPTADO;
                case "Pendientes" -> u.getEstado() == EstadoUsuario.PENDIENTE;
                case "Rechazados" -> u.getEstado() == EstadoUsuario.RECHAZADO;
                default -> true;
            };

            if (matchFiltro) listaFiltrada.add(u);
        }

        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        int total = listaUsuarios.size();
        int conectados = (int) listaUsuarios.stream().filter(Usuario::isConectado).count();

        lblTotal.setText("Total: " + total);
        lblConectados.setText("🟢 Conectados: " + conectados);
        lblDesconectados.setText("⚫ Desconectados: " + (total - conectados));
    }

    @FXML
    private void handleRefrescar() {
        filtrarUsuarios();
    }

    @FXML
    private void handleVerDetalles() {
        Usuario u = tableUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Por favor selecciona un usuario.");
            alert.showAndWait();
            return;
        }
        mostrarDetallesUsuario(u);
    }

    private void mostrarDetallesUsuario(Usuario u) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Usuario");
        alert.setHeaderText("Información de " + u.getUsername());

        VBox box = new VBox(10);
        box.getChildren().addAll(
                new Label("Username: " + u.getUsername()),
                new Label("Email: " + (u.getEmail() != null ? u.getEmail() : "N/A")),
                new Label("Estado: " + u.getEstadoCuenta()),
                new Label("Conectado: " + (u.isConectado() ? "Sí" : "No")),
                new Label("IP: " + (u.getDireccionIP() != null ? u.getDireccionIP() : "N/A")),
                new Label("Enviados: " + u.getMensajesEnviados()),
                new Label("Recibidos: " + u.getMensajesRecibidos()),
                new Label("Última conexión: " +
                        (u.getFechaUltimaConexion() != null
                                ? u.getFechaUltimaConexion().format(dateFormatter)
                                : "Nunca")),
                new Label("Registro: " +
                        (u.getFechaRegistro() != null
                                ? u.getFechaRegistro().format(dateFormatter)
                                : "N/A"))
        );

        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    private void actualizarBotonesSeleccion() {
        btnVerDetalles.setDisable(
                tableUsuarios.getSelectionModel().getSelectedItem() == null);
    }

    public void limpiar() {
        Platform.runLater(() -> {
            listaUsuarios.clear();
            listaFiltrada.clear();
            actualizarEstadisticas();
        });
    }

    public Usuario getUsuarioSeleccionado() {
        return tableUsuarios.getSelectionModel().getSelectedItem();
    }
}
