package Backend.Server.Controladores.Web;

import Backend.Server.Repositorios.UsuarioRepository;
import Backend.Server.Utils.Dto.UsuarioListadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/informes/usuarios")
@RequiredArgsConstructor
public class UsuariosController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarUsuarios(Model model) {

        var lista = usuarioRepository.findAll().stream()
                .map(u -> new UsuarioListadoDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.isActivo(),
                        u.isConectado(),
                        u.getFechaRegistro(),
                        u.getFechaUltimaConexion()
                ))
                .toList();

        model.addAttribute("usuarios", lista);
        return "informes/usuarios";
    }
}
