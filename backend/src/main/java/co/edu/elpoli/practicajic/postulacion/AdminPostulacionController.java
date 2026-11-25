package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.oferta.OfertaDtos.AsignarEvaluadorRequest;
import co.edu.elpoli.practicajic.oferta.OfertaService;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.PostulanteDto;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import co.edu.elpoli.practicajic.usuario.Rol;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración · Postulaciones y evaluadores", description = "RF-34")
public class AdminPostulacionController {

    public record EvaluadorDto(Long id, String nombre, String correo) {
    }

    private final PostulacionService postulaciones;
    private final OfertaService ofertas;
    private final UsuarioRepository usuarios;

    public AdminPostulacionController(PostulacionService postulaciones, OfertaService ofertas, UsuarioRepository usuarios) {
        this.postulaciones = postulaciones;
        this.ofertas = ofertas;
        this.usuarios = usuarios;
    }

    @GetMapping("/evaluadores")
    @Operation(summary = "Miembros activos del comité que se pueden asignar como evaluadores")
    public List<EvaluadorDto> evaluadores() {
        return usuarios.findByRolAndActivoTrueOrderByNombreAsc(Rol.COMITE).stream()
                .map(u -> new EvaluadorDto(u.getId(), u.getNombre(), u.getCorreo())).toList();
    }

    @GetMapping("/postulaciones")
    @Operation(summary = "Listar postulaciones, opcionalmente por estado")
    public List<PostulanteDto> listar(@RequestParam(required = false) EstadoPostulacion estado) {
        return postulaciones.todas(estado);
    }

    @PutMapping("/postulaciones/{id}/evaluador")
    @Transactional
    @Operation(summary = "Asignar el miembro del comité que califica al candidato")
    public PostulanteDto asignar(@PathVariable Long id, @Valid @RequestBody AsignarEvaluadorRequest datos,
                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return postulaciones.asignarEvaluador(id, ofertas.evaluadorValido(datos.evaluadorId()), actual.id());
    }
}
