package Backend.Server.Controladores.Web;

import Backend.Server.Servicios.Core.ArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileInputStream;

@Controller
@RequiredArgsConstructor
public class ArchivoDescargaController {

    private final ArchivoService archivoService;

    @GetMapping("/informes/archivos/descargar/{id}")
    public ResponseEntity<?> descargar(@PathVariable Long id) {
        try {
            var archivo = archivoService.obtenerPorId(id);
            if (archivo == null) {
                return ResponseEntity.badRequest().body("Archivo no encontrado");
            }

            File file = new File(archivo.getRutaAlmacenamiento());
            if (!file.exists()) {
                return ResponseEntity.badRequest().body("El archivo físico no existe");
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + archivo.getNombreArchivo() + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error descargando archivo: " + e.getMessage());
        }
    }
}
