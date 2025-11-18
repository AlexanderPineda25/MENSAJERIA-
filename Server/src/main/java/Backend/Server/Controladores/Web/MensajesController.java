package Backend.Server.Controladores.Web;

import Backend.Server.Repositorios.ArchivoRepository;
import Backend.Server.Repositorios.MensajeRepository;
import Backend.Server.Utils.Dto.MensajeDetalleDTO;
import Backend.Server.Utils.Dto.MensajeListadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/informes/mensajes")
@RequiredArgsConstructor
public class MensajesController {

    private final MensajeRepository mensajeRepository;
    private final ArchivoRepository archivoRepository;

    @GetMapping
    public String listarMensajes(Model model) {

        var lista = mensajeRepository.findAllWithUsuarios().stream()
                .map(m -> new MensajeListadoDTO(
                        m.getId(),
                        m.getRemitente().getUsername(),
                        m.getDestinatario().getUsername(),
                        m.getTipo().name(),
                        m.getFechaHoraEnvio()
                ))
                .toList();

        model.addAttribute("mensajes", lista);
        return "informes/mensajes";
    }

    @GetMapping("/{id}")
    public String detalleMensaje(@PathVariable Long id, Model model) {

        var mensaje = mensajeRepository.findByIdWithArchivo(id);
        if (mensaje == null) {
            model.addAttribute("error", "Mensaje no encontrado");
            return "informes/mensaje-detalle";
        }

        MensajeDetalleDTO dto = new MensajeDetalleDTO(
                mensaje.getId(),
                mensaje.getRemitente().getUsername(),
                mensaje.getDestinatario().getUsername(),
                mensaje.getContenido(),
                mensaje.getFechaHoraEnvio(),
                mensaje.getTipo().name(),
                mensaje.getArchivo() != null ? mensaje.getArchivo().getId() : null,
                mensaje.getArchivo() != null ? mensaje.getArchivo().getNombreArchivo() : null,
                mensaje.getArchivo() != null ? mensaje.getArchivo().getTamanoBytes() : null
        );

        model.addAttribute("detalle", dto);
        return "informes/mensaje-detalle";
    }
}
