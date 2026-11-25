package co.edu.elpoli.practicajic.admin;

import co.edu.elpoli.practicajic.admin.dto.CambiarEstadoRequest;
import co.edu.elpoli.practicajic.admin.dto.CrearUsuarioRequest;
import co.edu.elpoli.practicajic.admin.dto.EditarUsuarioRequest;
import co.edu.elpoli.practicajic.admin.dto.UsuarioAdminResponse;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import co.edu.elpoli.practicajic.usuario.Rol;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Ruta bajo /api/admin: solo accesible con rol ADMIN (ver SeguridadConfig). */
@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administración · Usuarios y roles", description = "RF-05")
public class UsuarioAdminController {

    private final UsuarioAdminService servicio;

    public UsuarioAdminController(UsuarioAdminService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios con filtros por rol, estado y texto")
    public Page<UsuarioAdminResponse> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false, name = "q") String busqueda,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pagina) {
        return servicio.listar(rol, activo, busqueda, pagina);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un administrador o miembro del comité")
    public UsuarioAdminResponse crear(@Valid @RequestBody CrearUsuarioRequest datos,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.crear(datos, actual.id());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar correo, nombre y rol de un usuario")
    public UsuarioAdminResponse editar(@PathVariable Long id, @Valid @RequestBody EditarUsuarioRequest datos,
                                       @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.editar(id, datos, actual.id());
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un usuario (al desactivarlo se cierran sus sesiones)")
    public UsuarioAdminResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest datos,
                                              @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.cambiarEstado(id, datos, actual.id());
    }
}
