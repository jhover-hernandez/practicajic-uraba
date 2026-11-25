package co.edu.elpoli.practicajic.validacion;

import co.edu.elpoli.practicajic.archivo.DescargaPdf;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import co.edu.elpoli.practicajic.empresa.EstadoValidacionEmpresa;
import co.edu.elpoli.practicajic.estudiante.EstadoEstudiante;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.PerfilEstudianteDto;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import co.edu.elpoli.practicajic.validacion.ValidacionDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/validaciones")
@Tag(name = "Administración · Validaciones", description = "RF-07 y RF-16")
public class ValidacionController {

    private final ValidacionService servicio;

    public ValidacionController(ValidacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/empresas")
    @Operation(summary = "Listar empresas, opcionalmente por estado de validación")
    public List<PerfilEmpresaDto> empresas(@RequestParam(required = false) EstadoValidacionEmpresa estado) {
        return servicio.empresas(estado);
    }

    @PatchMapping("/empresas/{id}")
    @Operation(summary = "Validar, rechazar o inactivar una empresa")
    public PerfilEmpresaDto decidirEmpresa(@PathVariable Long id, @Valid @RequestBody DecisionRequest datos,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.decidirEmpresa(id, datos, actual.id());
    }

    @GetMapping("/estudiantes")
    @Operation(summary = "Listar estudiantes, opcionalmente por estado")
    public List<EstudianteResumenDto> estudiantes(@RequestParam(required = false) EstadoEstudiante estado) {
        return servicio.estudiantes(estado);
    }

    @GetMapping("/estudiantes/{id}")
    @Operation(summary = "Ver el perfil digital completo de un estudiante")
    public PerfilEstudianteDto estudiante(@PathVariable Long id) {
        return servicio.estudiante(id);
    }

    @GetMapping("/estudiantes/{id}/hoja-vida")
    @Operation(summary = "Ver la hoja de vida activa de un estudiante")
    public ResponseEntity<Resource> hojaVida(@PathVariable Long id) {
        var hoja = servicio.hojaVidaEstudiante(id);
        return DescargaPdf.de(hoja.archivo(), hoja.nombreOriginal());
    }

    @PatchMapping("/estudiantes/{id}")
    @Operation(summary = "Validar, rechazar o inactivar un estudiante")
    public EstudianteResumenDto decidirEstudiante(@PathVariable Long id, @Valid @RequestBody DecisionRequest datos,
                                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.decidirEstudiante(id, datos, actual.id());
    }
}
