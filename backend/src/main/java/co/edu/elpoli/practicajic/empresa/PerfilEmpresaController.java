package co.edu.elpoli.practicajic.empresa;

import co.edu.elpoli.practicajic.empresa.EmpresaDtos.ActualizarEmpresaRequest;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Ruta bajo /api/empresa: solo accesible con rol EMPRESA. */
@RestController
@RequestMapping("/api/empresa/perfil")
@Tag(name = "Empresa · Perfil", description = "RF-06")
public class PerfilEmpresaController {

    private final PerfilEmpresaService servicio;

    public PerfilEmpresaController(PerfilEmpresaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Ver el perfil de mi empresa")
    public PerfilEmpresaDto ver(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.ver(actual.id());
    }

    @PutMapping
    @Operation(summary = "Actualizar el perfil de mi empresa")
    public PerfilEmpresaDto actualizar(@Valid @RequestBody ActualizarEmpresaRequest datos,
                                       @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.actualizar(actual.id(), datos);
    }
}
