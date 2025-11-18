package Backend.Server.Controladores;

import Backend.Server.Repositorios.ArchivoRepository;
import Backend.Server.Repositorios.ConexionRepository;
import Backend.Server.Repositorios.MensajeRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

@Component("informesDesktopController")
@RequiredArgsConstructor
public class InformesDesktopController {

    @FXML private Label lblUsuarioActivo;
    @FXML private Label lblArchivoGrande;
    @FXML private Label lblConexiones;
    @FXML private Label lblUltimaAccion;

    private final MensajeRepository mensajeRepository;
    private final ArchivoRepository archivoRepository;
    private final ConexionRepository conexionRepository;

    @FXML
    private void initialize() {
        actualizarInformes();
    }

    @FXML
    private void actualizarInformes() {
        Platform.runLater(() -> {
            var usuarioMasActivo = mensajeRepository.findAll().stream()
                    .filter(m -> m.getRemitente() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            m -> m.getRemitente().getUsername(),
                            java.util.stream.Collectors.counting()
                    ))
                    .entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .orElse(null);

            if (usuarioMasActivo != null) {
                lblUsuarioActivo.setText(usuarioMasActivo.getKey() + " (" + usuarioMasActivo.getValue() + " msgs)");
            } else {
                lblUsuarioActivo.setText("N/A");
            }

            var archivo = archivoRepository.findAll().stream()
                    .max(Comparator.comparingLong(a -> a.getTamanoBytes()))
                    .orElse(null);

            if (archivo != null) {
                lblArchivoGrande.setText(archivo.getNombreArchivo() + " (" + formatoBytes(archivo.getTamanoBytes()) + ")");
            } else {
                lblArchivoGrande.setText("N/A");
            }

            lblConexiones.setText(String.valueOf(conexionRepository.findByActivaTrue().size()));

            lblUltimaAccion.setText(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        });
    }

    private String formatoBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
