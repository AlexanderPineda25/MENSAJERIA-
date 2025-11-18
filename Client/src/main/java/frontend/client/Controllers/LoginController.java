package frontend.client.Controllers;

import Shared.Mensaje;
import frontend.client.network.ConexionCliente;
import frontend.client.network.DetectorIP;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LoginController {

    @FXML private Label lblIP;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtIPServidor;
    @FXML private Button btnLogin;
    @FXML private Button btnRegistro;
    @FXML private Label lblEstado;

    private String ipCliente;
    private ConexionCliente conexion;
    private CompletableFuture<String> respuestaFuture;

    @FXML
    public void initialize() {
        detectarIP();
        if (txtIPServidor.getText().isEmpty()) {
            txtIPServidor.setText("localhost");
        }

        txtUsuario.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> handleLogin());
    }

    private void detectarIP() {
        new Thread(() -> {
            ipCliente = DetectorIP.obtenerIPLocal();
            Platform.runLater(() -> {
                lblIP.setText(ipCliente);
                lblIP.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            });
        }).start();
    }

    @FXML
    private void handleLogin() {
        conectarServidor(null);
    }

    @FXML
    void conectarServidor(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        String ipServidor = txtIPServidor.getText().trim();

        if (usuario.isEmpty()) {
            mostrarError("Por favor ingrese su usuario");
            return;
        }
        if (password.isEmpty()) {
            mostrarError("Por favor ingrese su contraseña");
            return;
        }
        if (ipServidor.isEmpty()) {
            mostrarError("Por favor ingrese la IP del servidor");
            return;
        }

        deshabilitarBotones(true);
        lblEstado.setText("Conectando al servidor...");
        lblEstado.setStyle("-fx-text-fill: #f39c12;");

        new Thread(() -> {
            try {
                conexion = new ConexionCliente();
                respuestaFuture = new CompletableFuture<>();

                conexion.conectar(ipServidor, 5000, this::procesarRespuestaLogin);

                // Esperar conexión real
                while (!conexion.estaConectado()) {
                    Thread.sleep(50);
                }

                conexion.enviarLogin(usuario, password, ipCliente);

                String respuesta = respuestaFuture.get(5, TimeUnit.SECONDS);

                Platform.runLater(() -> manejarRespuestaLogin(respuesta, usuario));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("Error al conectar: " + e.getMessage());
                    deshabilitarBotones(false);
                });
            }
        }).start();
    }

    private void procesarRespuestaLogin(Mensaje mensaje) {
        if (mensaje != null && "LOGIN_RESPUESTA".equals(mensaje.getTipo())) {
            respuestaFuture.complete(mensaje.getContenido());
        }
    }

    private void manejarRespuestaLogin(String respuesta, String usuario) {
        switch (respuesta) {
            case "LOGIN_OK":
                abrirChatView(usuario);
                break;

            case "USUARIO_PENDIENTE":
                mostrarError("Usuario pendiente de aprobación");
                recuperarse();
                break;

            case "USUARIO_RECHAZADO":
                mostrarError("Usuario rechazado por el administrador");
                recuperarse();
                break;

            default:
                mostrarError("Usuario o contraseña incorrectos");
                recuperarse();
        }
    }

    private void recuperarse() {
        deshabilitarBotones(false);
        if (conexion != null) conexion.cerrar();
    }

    private void abrirChatView(String usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/client/ChatView.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.inicializar(usuario, ipCliente, conexion);

            Stage stage = new Stage();
            stage.setTitle("Chat - " + usuario);
            stage.setScene(new Scene(root, 800, 600));

            stage.setOnCloseRequest(e -> chatController.limpiarRecursos());
            stage.show();

            ((Stage) btnLogin.getScene().getWindow()).close();

        } catch (Exception e) {
            mostrarError("Error al abrir chat: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/client/RegistroView.fxml"));
            Parent root = loader.load();

            RegistroController reg = loader.getController();
            reg.setIPCliente(ipCliente);
            reg.setIPServidor(txtIPServidor.getText());

            Stage stage = new Stage();
            stage.setTitle("Registro de Usuario");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            mostrarError("Error al abrir registro: " + e.getMessage());
        }
    }

    private void deshabilitarBotones(boolean b) {
        btnLogin.setDisable(b);
        btnRegistro.setDisable(b);
        txtUsuario.setDisable(b);
        txtPassword.setDisable(b);
        txtIPServidor.setDisable(b);
    }

    private void mostrarError(String msg) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-text-fill: #e74c3c;");
    }
}
