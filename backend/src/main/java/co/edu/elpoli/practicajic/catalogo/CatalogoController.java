package co.edu.elpoli.practicajic.catalogo;

import co.edu.elpoli.practicajic.catalogo.CatalogoDtos.*;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consulta para cualquier usuario con sesión; la edición queda bajo /api/admin (solo ADMIN). */
@RestController
@Tag(name = "Catálogos", description = "Áreas y competencias (RF-21)")
public class CatalogoController {

    private final CatalogoService servicio;

    public CatalogoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/api/catalogos/areas")
    @Operation(summary = "Listar áreas")
    public List<AreaDto> areas() {
        return servicio.areas();
    }

    @GetMapping("/api/catalogos/competencias")
    @Operation(summary = "Listar competencias")
    public List<CompetenciaDto> competencias() {
        return servicio.competencias();
    }

    @PostMapping("/api/admin/catalogos/areas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear área")
    public AreaDto crearArea(@Valid @RequestBody AreaRequest datos, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.crearArea(datos, actual.id());
    }

    @PutMapping("/api/admin/catalogos/areas/{id}")
    @Operation(summary = "Renombrar área")
    public AreaDto editarArea(@PathVariable Long id, @Valid @RequestBody AreaRequest datos,
                              @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.editarArea(id, datos, actual.id());
    }

    @DeleteMapping("/api/admin/catalogos/areas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar área sin ofertas")
    public void eliminarArea(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        servicio.eliminarArea(id, actual.id());
    }

    @PostMapping("/api/admin/catalogos/competencias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear competencia")
    public CompetenciaDto crearCompetencia(@Valid @RequestBody CompetenciaRequest datos,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.crearCompetencia(datos, actual.id());
    }

    @PutMapping("/api/admin/catalogos/competencias/{id}")
    @Operation(summary = "Editar competencia")
    public CompetenciaDto editarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaRequest datos,
                                            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.editarCompetencia(id, datos, actual.id());
    }

    @DeleteMapping("/api/admin/catalogos/competencias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar competencia sin uso")
    public void eliminarCompetencia(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        servicio.eliminarCompetencia(id, actual.id());
    }
}
