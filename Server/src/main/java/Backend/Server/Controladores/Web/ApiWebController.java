package Backend.Server.Controladores.Web;

import Backend.Server.Repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ApiWebController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/api/web")
    public String paginaApi(Model model) {

        var usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);

        return "api/index";
    }
}
