package frontend.client.Controllers;

import frontend.client.network.ConexionCliente;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RegistroController {

    @FXML private Label lblIPCliente;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private TextField txtIPServidor;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;
    @FXML private Label lblEstado;

    private String ipCliente;
    private String ipServidor;
    private CompletableFuture<String> respuestaFuture;

    @FXML
    public void initialize() {
        txtUsuario.setOnAction(e -> txtEmail.requestFocus());
        txtEmail.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> txtConfirmarPassword.requestFocus());
        txtConfirmarPassword.setOnAction(e -> handleRegistrar());
    }

    public void setIPCliente(String ip) {
        this.ipCliente = ip;
        lblIPCliente.setText(ip);
    }

    public void setIPServidor(String ip) {
        this.ipServidor = ip;
        txtIPServidor.setText(ip);
    }

    @FXML
    private void handleRegistrar() {

        String usuario = txtUsuario.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmarPassword = txtConfirmarPassword.getText();
        String ipServ = txtIPServidor.getText().trim();

        if (usuario.isEmpty() || email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return;
        }

        if (usuario.length() < 4) {
            mostrarError("El usuario debe tener al menos 4 caracteres");
            txtUsuario.requestFocus();
            return;
        }

        if (!esEmailValido(email)) {
            mostrarError("El formato del email no es válido");
            txtEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            txtPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden");
            txtConfirmarPassword.clear();
            txtConfirmarPassword.requestFocus();
            return;
        }

        if (ipServ.isEmpty()) {
            mostrarError("Por favor ingrese la IP del servidor");
            txtIPServidor.requestFocus();
            return;
        }

        deshabilitarBotones(true);
        lblEstado.setText("Registrando usuario...");
        lblEstado.setStyle("-fx-text-fill: #f39c12;");

        new Thread(() -> {
            ConexionCliente conexion = null;
            try {
                conexion = new ConexionCliente();
                respuestaFuture = new CompletableFuture<>();

                ConexionCliente finalConexion = conexion;
                conexion.conectar(ipServ, 5000, mensaje -> {
                    if (mensaje != null && "REGISTRO_RESPUESTA".equals(mensaje.getTipo())) {
                        respuestaFuture.complete(mensaje.getContenido());
                    }
                });

                // Esperar conexión REAL
                while (!conexion.estaConectado()) {
                    Thread.sleep(50);
                }

                // Enviar datos
                conexion.enviarRegistro(usuario, email, password, ipCliente);

                String respuesta = respuestaFuture.get(5, TimeUnit.SECONDS);

                ConexionCliente finalConexion1 = conexion;
                Platform.runLater(() -> procesarRespuesta(respuesta, finalConexion1));

            } catch (Exception e) {
                ConexionCliente finalConexion2 = conexion;
                Platform.runLater(() -> {
                    mostrarError("Error al conectar: " + e.getMessage());
                    deshabilitarBotones(false);
                    if (finalConexion2 != null) finalConexion2.cerrar();
                });
            }
        }).start();
    }

    private void procesarRespuesta(String respuesta, ConexionCliente conexion) {
        switch (respuesta) {
            case "REGISTRO_OK":
                mostrarAlerta("Registro Exitoso",
                        "Usuario registrado correctamente.\n\n" +
                                "Tu cuenta está pendiente de aprobación por el administrador.",
                        Alert.AlertType.INFORMATION);
                conexion.cerrar();
                cerrarVentana();
                break;

            case "USUARIO_EXISTE":
                mostrarError("El usuario ya existe");
                break;

            case "EMAIL_EXISTE":
                mostrarError("El email ya está registrado");
                break;

            default:
                mostrarError("Error: " + respuesta);
        }

        deshabilitarBotones(false);
        conexion.cerrar();
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private boolean esEmailValido(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void deshabilitarBotones(boolean d) {
        btnRegistrar.setDisable(d);
        btnCancelar.setDisable(d);
        txtUsuario.setDisable(d);
        txtEmail.setDisable(d);
        txtPassword.setDisable(d);
        txtConfirmarPassword.setDisable(d);
        txtIPServidor.setDisable(d);
    }

    private void mostrarError(String msg) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
