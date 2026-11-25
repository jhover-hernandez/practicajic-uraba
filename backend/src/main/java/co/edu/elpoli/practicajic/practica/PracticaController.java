package co.edu.elpoli.practicajic.practica;

import co.edu.elpoli.practicajic.practica.PracticaDtos.*;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cada ruta vive bajo el prefijo de su rol, así SeguridadConfig la protege (RNF-03). */
@RestController
@Tag(name = "Prácticas y seguimiento", description = "RF-11 y RF-35 a RF-37")
public class PracticaController {

    private final PracticaService servicio;

    public PracticaController(PracticaService servicio) {
        this.servicio = servicio;
    }

    // ---------- Administrador ----------

    @GetMapping("/api/admin/practicas/pendientes")
    @Operation(summary = "Estudiantes que aceptaron la práctica y esperan formalización")
    public List<PendienteFormalizarDto> pendientes() {
        return servicio.pendientesDeFormalizar();
    }

    @PostMapping("/api/admin/practicas/formalizar/{postulacionId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Formalizar la práctica: fechas, tutores y vinculación")
    public PracticaDto formalizar(@PathVariable Long postulacionId, @Valid @RequestBody FormalizarRequest datos,
                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.formalizar(postulacionId, datos, actual.id());
    }

    @GetMapping("/api/admin/practicas")
    @Operation(summary = "Listar prácticas, opcionalmente por estado")
    public List<PracticaDto> listar(@RequestParam(required = false) EstadoPractica estado) {
        return servicio.todas(estado);
    }

    @GetMapping("/api/admin/practicas/{id}")
    @Operation(summary = "Detalle de una práctica con sus seguimientos")
    public PracticaDto detalle(@PathVariable Long id) {
        return servicio.detalle(id);
    }

    @PostMapping("/api/admin/practicas/{id}/seguimientos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar seguimiento o evaluación final (Politécnico)")
    public PracticaDto seguimientoAdmin(@PathVariable Long id, @Valid @RequestBody SeguimientoRequest datos,
                                        @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.registrarComoAdmin(id, datos, actual.id());
    }

    // ---------- Empresa ----------

    @GetMapping("/api/empresa/practicas")
    @Operation(summary = "Prácticas de mi empresa")
    public List<PracticaDto> deEmpresa(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.deEmpresa(actual.id());
    }

    @PostMapping("/api/empresa/practicas/{id}/seguimientos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Evaluar al practicante durante o al final de la práctica")
    public PracticaDto seguimientoEmpresa(@PathVariable Long id, @Valid @RequestBody SeguimientoRequest datos,
                                          @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.registrarComoEmpresa(actual.id(), id, datos);
    }

    // ---------- Estudiante ----------

    @GetMapping("/api/estudiante/practicas")
    @Operation(summary = "Mi práctica y sus seguimientos")
    public List<PracticaDto> delEstudiante(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.delEstudiante(actual.id());
    }
}
