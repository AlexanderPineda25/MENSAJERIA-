package Backend.Server.Controladores.Web;

import Backend.Server.Repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/informes")
@RequiredArgsConstructor
public class InformesController {

    private final UsuarioRepository usuarioRepository;
    private final MensajeRepository mensajeRepository;
    private final ArchivoRepository archivoRepository;
    private final ConexionRepository conexionRepository;

    @GetMapping("/usuario-mas-activo")
    public String usuarioMasActivo(Model model) {

        var top = mensajeRepository.obtenerUsuarioMasActivo();

        model.addAttribute("topUsuario", top);
        return "informes/usuario-mas-activo";
    }

    @GetMapping("/archivos")
    public String archivosPorTamano(Model model) {
        var archivos = archivoRepository.findAllOrdenadosPorTamano();
        model.addAttribute("archivos", archivos);
        return "informes/archivos";
    }

    @GetMapping("/conectados")
    public String usuariosConectados(Model model) {
        var conectados = conexionRepository.findUsuariosConectados();
        model.addAttribute("conectados", conectados);
        return "informes/conectados";
    }

    @GetMapping("/desconectados")
    public String usuariosDesconectados(Model model) {
        var desconectados = conexionRepository.findUsuariosDesconectados();
        model.addAttribute("desconectados", desconectados);
        return "informes/desconectados";
    }

    @GetMapping
    public String index() {
        return "informes/index";
    }
}
