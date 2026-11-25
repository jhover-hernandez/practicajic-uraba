package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.oferta.OfertaDtos.AsignarEvaluadorRequest;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.OfertaDto;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ofertas")
@Tag(name = "Administración · Convocatorias", description = "RF-34 y gestión de convocatorias")
public class AdminOfertaController {

    private final OfertaService servicio;

    public AdminOfertaController(OfertaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar todas las ofertas, opcionalmente por estado")
    public List<OfertaDto> listar(@RequestParam(required = false) EstadoOferta estado) {
        return servicio.todas(estado);
    }

    @PutMapping("/{id}/evaluador")
    @Operation(summary = "Asignar el miembro del comité que evalúa la oferta")
    public OfertaDto asignar(@PathVariable Long id, @Valid @RequestBody AsignarEvaluadorRequest datos,
                             @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.asignarEvaluador(id, datos, actual.id());
    }

    @PostMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar una convocatoria abierta")
    public OfertaDto cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.cerrarAdmin(id, actual.id());
    }
}
