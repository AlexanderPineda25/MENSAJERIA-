package Backend.Server.Controladores.Web;

import Backend.Server.Repositorios.MensajeRepository;
import Backend.Server.Servicios.Core.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/informes/usuarios")
@RequiredArgsConstructor
public class UsuarioDetalleController {

    private final UsuarioService usuarioService;
    private final MensajeRepository mensajeRepository;

    @GetMapping("/{id}")
    public String detalleUsuario(@PathVariable Long id, Model model) {

        var info = usuarioService.obtenerInfoCompleta(id);
        var enviados = mensajeRepository.findMensajesEnviadosByUsuarioId(id);
        var recibidos = mensajeRepository.findMensajesRecibidosByUsuarioId(id);

        model.addAttribute("info", info);
        model.addAttribute("enviados", enviados);
        model.addAttribute("recibidos", recibidos);

        return "informes/usuario-detalle";
    }
}
