package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.oferta.OfertaDtos.OfertaDto;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.OfertaRequest;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresa/ofertas")
@Tag(name = "Empresa · Ofertas", description = "RF-08 y RF-09")
public class EmpresaOfertaController {

    private final OfertaService servicio;

    public EmpresaOfertaController(OfertaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar mis ofertas con su estado y número de postulados")
    public List<OfertaDto> listar(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.deEmpresa(actual.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una de mis ofertas")
    public OfertaDto ver(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.propia(actual.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear oferta como borrador o enviarla de una vez al comité")
    public OfertaDto crear(@Valid @RequestBody OfertaRequest datos, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.crear(actual.id(), datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar una oferta en borrador o con ajustes solicitados")
    public OfertaDto editar(@PathVariable Long id, @Valid @RequestBody OfertaRequest datos,
                            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.editar(actual.id(), id, datos);
    }

    @PostMapping("/{id}/enviar")
    @Operation(summary = "Enviar la oferta a revisión del comité")
    public OfertaDto enviar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.enviar(actual.id(), id);
    }

    @PostMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar una convocatoria abierta")
    public OfertaDto cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.cerrarPropia(actual.id(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un borrador")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        servicio.eliminar(actual.id(), id);
    }
}
