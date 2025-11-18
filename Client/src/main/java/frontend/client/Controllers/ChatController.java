package frontend.client.Controllers;

import Shared.Mensaje;
import frontend.client.network.ConexionCliente;
import frontend.client.ui.MensajeUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChatController {

    @FXML private Label lblUsuario;
    @FXML private Label lblIP;
    @FXML private ListView<String> listUsuarios;
    @FXML private TextField txtMensaje;
    @FXML private Button btnArchivo;
    @FXML private Button btnDesconectar;
    @FXML private ListView<MensajeUI> listMensajes;

    private String usuarioActual;
    private String ipCliente;
    private ConexionCliente conexion;
    private String usuarioSeleccionado;

    public void inicializar(String usuario, String ip, ConexionCliente conexion) {
        this.usuarioActual = usuario;
        this.ipCliente = ip;
        this.conexion = conexion;

        lblUsuario.setText("Usuario: " + usuario);
        lblIP.setText("IP: " + ip);

        this.conexion.actualizarListener(this::procesarMensaje);
        listMensajes.setCellFactory(this::crearCeldaMensaje);

        solicitarUsuarios();

        listUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                usuarioSeleccionado = newVal;
                listMensajes.getItems().clear();
                agregarMensajeSistema("--- Chat con " + newVal + " ---");
                try {
                    conexion.solicitarHistorial(usuarioActual, usuarioSeleccionado);
                } catch (Exception e) {
                    mostrarError("No se pudo solicitar historial: " + e.getMessage());
                }
            }
        });

        txtMensaje.setOnAction(e -> enviarMensaje());
        agregarMensajeSistema("Bienvenido al chat, " + usuario + "!");
    }

    private void solicitarUsuarios() {
        new Thread(() -> {
            try {
                conexion.solicitarUsuariosConectados(usuarioActual);
            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error al solicitar usuarios: " + e.getMessage()));
            }
        }).start();
    }

    private void actualizarListaUsuarios(String[] usuarios) {
        if (usuarios == null) return;

        listUsuarios.getItems().clear();

        for (String u : usuarios) {
            if (!u.equals(usuarioActual)) {
                listUsuarios.getItems().add(u);
            }
        }

        agregarMensajeSistema("Usuarios conectados: " + listUsuarios.getItems().size());
    }

    @FXML
    private void enviarMensaje() {
        String mensaje = txtMensaje.getText().trim();
        if (mensaje.isEmpty()) {
            mostrarError("Escribe un mensaje");
            return;
        }
        if (usuarioSeleccionado == null) {
            mostrarError("Selecciona un usuario");
            return;
        }

        new Thread(() -> {
            try {
                conexion.enviarMensajeTexto(usuarioSeleccionado, mensaje, ipCliente, usuarioActual);

                Platform.runLater(() -> {
                    MensajeUI ui = new MensajeUI(MensajeUI.Tipo.TEXTO, usuarioActual, true);
                    ui.setContenido(mensaje);
                    agregarMensajeUI(ui);
                    txtMensaje.clear();
                });

            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error al enviar: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void enviarArchivo() {
        if (usuarioSeleccionado == null) {
            mostrarError("Selecciona un usuario");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo");

        File archivo = fc.showOpenDialog(btnArchivo.getScene().getWindow());
        if (archivo == null) return;

        if (archivo.length() > 50L * 1024 * 1024) {
            mostrarError("El archivo es demasiado grande (máximo 50MB)");
            return;
        }

        new Thread(() -> {
            try {
                conexion.enviarArchivo(usuarioSeleccionado, archivo, ipCliente, usuarioActual);

                Platform.runLater(() -> {
                    MensajeUI ui = new MensajeUI(MensajeUI.Tipo.ARCHIVO, usuarioActual, true);
                    ui.setNombreArchivo(archivo.getName());
                    ui.setTamano(archivo.length());
                    agregarMensajeUI(ui);
                });

            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error al enviar archivo: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void desconectar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Desconectar");
        alert.setHeaderText("¿Deseas desconectarte?");
        alert.setContentText("Se cerrará la sesión actual");

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                limpiarRecursos();
                ((Stage) btnDesconectar.getScene().getWindow()).close();
            }
        });
    }

    private void agregarMensajeUI(MensajeUI m) {
        listMensajes.getItems().add(m);
        listMensajes.scrollTo(listMensajes.getItems().size() - 1);
    }

    private void agregarMensajeSistema(String texto) {
        MensajeUI ui = new MensajeUI(MensajeUI.Tipo.TEXTO, "Sistema", false);
        ui.setContenido(texto);
        listMensajes.getItems().add(ui);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void limpiarRecursos() {
        if (conexion != null) conexion.cerrar();
    }

    private void procesarMensaje(Mensaje mensaje) {
        if (mensaje == null) return;

        Platform.runLater(() -> {
            switch (mensaje.getTipo()) {

                case "LISTAR_USUARIOS_RESPUESTA" ->
                        actualizarListaUsuarios(mensaje.getUsuariosConectados());

                case "MENSAJE_TEXTO" -> {
                    MensajeUI ui = new MensajeUI(
                            MensajeUI.Tipo.TEXTO,
                            mensaje.getRemitente(),
                            mensaje.getRemitente().equals(usuarioActual)
                    );
                    ui.setContenido(mensaje.getContenido());
                    agregarMensajeUI(ui);
                }

                case "MENSAJE_ARCHIVO" -> {
                    MensajeUI ui = new MensajeUI(
                            MensajeUI.Tipo.ARCHIVO,
                            mensaje.getRemitente(),
                            mensaje.getRemitente().equals(usuarioActual)
                    );
                    ui.setNombreArchivo(mensaje.getNombreArchivo());
                    ui.setTamano(mensaje.getTamanoArchivo());
                    ui.setArchivoId(mensaje.getArchivoId());
                    agregarMensajeUI(ui);
                }

                case "HISTORIAL_RESPUESTA" ->
                        mostrarHistorial(mensaje.getHistorial());

                case "DESCARGAR_ARCHIVO_RESPUESTA" ->
                        guardarArchivoRecibido(mensaje);

                case "ERROR" ->
                        mostrarError(mensaje.getContenido());

                default ->
                        agregarMensajeSistema("Mensaje desconocido: " + mensaje.getTipo());
            }
        });
    }

    private void mostrarHistorial(Mensaje[] historial) {
        listMensajes.getItems().clear();

        if (historial == null) return;

        for (Mensaje m : historial) {

            MensajeUI ui = new MensajeUI(
                    m.getTipo().equals("MENSAJE_ARCHIVO") ? MensajeUI.Tipo.ARCHIVO : MensajeUI.Tipo.TEXTO,
                    m.getRemitente(),
                    m.getRemitente().equals(usuarioActual)
            );

            if (ui.getTipo() == MensajeUI.Tipo.ARCHIVO) {
                ui.setArchivoId(m.getArchivoId());
                ui.setNombreArchivo(m.getNombreArchivo());
                ui.setTamano(m.getTamanoArchivo());
                ui.setContenidoArchivo(m.getContenidoArchivo());
            }

            if (ui.getTipo() == MensajeUI.Tipo.TEXTO) {
                ui.setContenido(m.getContenido());
            } else {
                ui.setNombreArchivo(m.getNombreArchivo());
                ui.setTamano(m.getTamanoArchivo());
                ui.setArchivoId(m.getArchivoId());
                ui.setContenidoArchivo(m.getContenidoArchivo());  // ⭐ FIX IMPORTANTE
            }

            agregarMensajeUI(ui);
        }
    }

    private void guardarArchivoRecibido(Mensaje mensaje) {
        if (mensaje.getContenidoArchivo() == null) {
            mostrarError("El archivo no pudo descargarse");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar archivo");
        fc.setInitialFileName(mensaje.getNombreArchivo());

        File destino = fc.showSaveDialog(listMensajes.getScene().getWindow());
        if (destino == null) return;

        try (FileOutputStream fos = new FileOutputStream(destino)) {
            fos.write(mensaje.getContenidoArchivo());
            agregarMensajeSistema("📥 Archivo guardado en: " + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarError("Error al guardar archivo: " + e.getMessage());
        }
    }

    private void guardarArchivoDirecto(MensajeUI m) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(m.getNombreArchivo());
        File destino = fc.showSaveDialog(listMensajes.getScene().getWindow());
        if (destino == null) return;

        try (FileOutputStream fos = new FileOutputStream(destino)) {
            fos.write(m.getContenidoArchivo());
            agregarMensajeSistema("📥 Archivo guardado en: " + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarError("Error guardando archivo: " + e.getMessage());
        }
    }

    private ListCell<MensajeUI> crearCeldaMensaje(ListView<MensajeUI> listView) {
        return new ListCell<>() {
            @Override
            protected void updateItem(MensajeUI m, boolean empty) {
                super.updateItem(m, empty);

                if (empty || m == null) {
                    setGraphic(null);
                    return;
                }

                HBox contenedor = new HBox();
                contenedor.setSpacing(10);

                VBox burbuja = new VBox();
                burbuja.setSpacing(3);
                burbuja.setPadding(new Insets(10));
                burbuja.setStyle("-fx-background-radius: 10; -fx-max-width: 400;");

                Label lblRemitente = new Label(m.getRemitente());
                lblRemitente.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

                if (m.getTipo() == MensajeUI.Tipo.TEXTO) {
                    Label lblMsg = new Label(m.getContenido());
                    lblMsg.setWrapText(true);
                    burbuja.getChildren().addAll(lblRemitente, lblMsg);
                } else {
                    Label lblArchivo = new Label("📎 " + m.getNombreArchivo() +
                            " (" + formatearTamano(m.getTamano()) + ")");
                    lblArchivo.setStyle("-fx-font-weight: bold;");

                    Button btnDescargar = new Button("Descargar");
                    btnDescargar.setOnAction(e -> {
                        if (m.getContenidoArchivo() != null) {
                            guardarArchivoDirecto(m);
                            return;
                        }
                        try {
                            conexion.solicitarDescargaArchivo(m.getArchivoId());
                        } catch (IOException ex) {
                            mostrarError("No se pudo descargar: " + ex.getMessage());
                        }
                    });

                    burbuja.getChildren().addAll(lblRemitente, lblArchivo, btnDescargar);
                }

                if (m.esMio()) {
                    burbuja.setStyle(burbuja.getStyle() +
                            "; -fx-background-color: #3498db; -fx-text-fill: white;");
                    contenedor.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    burbuja.setStyle(burbuja.getStyle() +
                            "; -fx-background-color: #bdc3c7;");
                    contenedor.setAlignment(Pos.CENTER_LEFT);
                }

                contenedor.getChildren().add(burbuja);
                setGraphic(contenedor);
            }
        };
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
