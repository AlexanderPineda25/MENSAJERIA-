package frontend.client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import javafx.scene.control.DialogPane;

import java.util.List;
import java.util.Optional;

/**
 * Clase utilitaria para mostrar alertas y diálogos en JavaFX
 */
public class AlertUtils {

    /**
     * Muestra una alerta de error
     */
    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error simple (sin título)
     */
    public static void mostrarError(String mensaje) {
        mostrarError("Error", mensaje);
    }

    /**
     * Muestra una alerta de información
     */
    public static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de información simple
     */
    public static void mostrarInformacion(String mensaje) {
        mostrarInformacion("Información", mensaje);
    }

    /**
     * Muestra una alerta de advertencia
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de advertencia simple
     */
    public static void mostrarAdvertencia(String mensaje) {
        mostrarAdvertencia("Advertencia", mensaje);
    }

    /**
     * Muestra una alerta de éxito (usando información con estilo)
     */
    public static void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("✓ Éxito");
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de éxito simple
     */
    public static void mostrarExito(String mensaje) {
        mostrarExito("Éxito", mensaje);
    }

    /**
     * Muestra un diálogo de confirmación
     * @return true si el usuario confirma, false si cancela
     */
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);

        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Muestra un diálogo de confirmación simple
     */
    public static boolean mostrarConfirmacion(String mensaje) {
        return mostrarConfirmacion("Confirmación", mensaje);
    }

    /**
     * Muestra un diálogo de confirmación personalizado
     */
    public static boolean mostrarConfirmacion(String titulo, String header, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);

        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Muestra un diálogo de confirmación con botones personalizados
     */
    public static Optional<ButtonType> mostrarConfirmacionConOpciones(
            String titulo,
            String mensaje,
            ButtonType... botones) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getButtonTypes().setAll(botones);
        aplicarEstilo(alert);

        return alert.showAndWait();
    }

    /**
     * Muestra un diálogo para ingresar texto
     */
    public static Optional<String> mostrarDialogoTexto(String titulo, String mensaje, String valorDefecto) {
        TextInputDialog dialog = new TextInputDialog(valorDefecto);
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(mensaje);
        aplicarEstiloDialogo(dialog);

        return dialog.showAndWait();
    }

    /**
     * Muestra un diálogo para ingresar texto simple
     */
    public static Optional<String> mostrarDialogoTexto(String mensaje) {
        return mostrarDialogoTexto("Entrada", mensaje, "");
    }

    /**
     * Muestra un diálogo de selección (ChoiceDialog)
     */
    public static <T> Optional<T> mostrarDialogoSeleccion(
            String titulo,
            String mensaje,
            List<T> opciones,
            T seleccionDefecto) {

        ChoiceDialog<T> dialog = new ChoiceDialog<>(seleccionDefecto, opciones);
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(mensaje);
        aplicarEstiloDialogo(dialog);

        return dialog.showAndWait();
    }

    /**
     * Muestra un diálogo de cargando/procesando
     */
    public static Alert mostrarDialogoCargando(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Procesando");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getButtonTypes().clear(); // Sin botones
        aplicarEstilo(alert);

        // No bloqueante
        alert.show();
        return alert;
    }

    /**
     * Muestra una alerta con detalles expandibles
     */
    public static void mostrarErrorConDetalles(String titulo, String mensaje, String detalles) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(detalles);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane.setVgrow(textArea, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.GridPane.setHgrow(textArea, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new javafx.scene.control.Label("Detalles:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de conexión perdida
     */
    public static void mostrarErrorConexion() {
        mostrarError(
                "Error de Conexión",
                "Se ha perdido la conexión con el servidor.\n\n" +
                        "Por favor verifica:\n" +
                        "- Tu conexión a internet\n" +
                        "- Que el servidor esté activo\n" +
                        "- La configuración de IP y puerto"
        );
    }

    /**
     * Muestra una alerta de timeout
     */
    public static void mostrarTimeout() {
        mostrarAdvertencia(
                "Tiempo de espera agotado",
                "El servidor no respondió a tiempo.\n\n" +
                        "Esto puede deberse a:\n" +
                        "- Problemas de red\n" +
                        "- Servidor sobrecargado\n" +
                        "- IP o puerto incorrectos"
        );
    }

    /**
     * Muestra una alerta de sesión expirada
     */
    public static void mostrarSesionExpirada() {
        mostrarAdvertencia(
                "Sesión Expirada",
                "Tu sesión ha expirado.\n\n" +
                        "Por favor, inicia sesión nuevamente."
        );
    }

    /**
     * Aplica estilo personalizado a las alertas
     */
    private static void aplicarEstilo(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();

        // Estilo CSS opcional
        dialogPane.setStyle(
                "-fx-background-color: #ecf0f1;" +
                        "-fx-font-size: 13px;"
        );

        // Hacer que el Stage sea siempre visible
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.setAlwaysOnTop(false);
    }

    /**
     * Aplica estilo a diálogos de texto/selección
     */
    private static void aplicarEstiloDialogo(javafx.scene.control.Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #ecf0f1;" +
                        "-fx-font-size: 13px;"
        );
    }

    /**
     * Muestra una notificación simple (sin bloquear)
     */
    public static void mostrarNotificacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);

        // Cerrar automáticamente después de 3 segundos
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(alert::close);
            } catch (InterruptedException e) {
                // Ignorar
            }
        }).start();

        alert.show();
    }
}