package co.edu.elpoli.practicajic.estudiante;

import co.edu.elpoli.practicajic.archivo.DescargaPdf;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.*;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Ruta bajo /api/estudiante: solo accesible con rol ESTUDIANTE. */
@RestController
@RequestMapping("/api/estudiante")
@Tag(name = "Estudiante · Perfil digital", description = "RF-12 a RF-15")
public class PerfilEstudianteController {

    private final PerfilEstudianteService servicio;

    public PerfilEstudianteController(PerfilEstudianteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/perfil")
    @Operation(summary = "Ver mi perfil digital completo")
    public PerfilEstudianteDto ver(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.ver(actual.id());
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar información personal y académica")
    public PerfilEstudianteDto actualizar(@Valid @RequestBody ActualizarPerfilRequest datos,
                                          @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.actualizar(actual.id(), datos);
    }

    @PostMapping(value = "/hoja-vida", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar o actualizar la hoja de vida (PDF, máximo 5 MB)")
    public PerfilEstudianteDto subirHojaVida(@RequestParam("archivo") MultipartFile archivo,
                                             @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.subirHojaVida(actual.id(), archivo);
    }

    @GetMapping("/hoja-vida")
    @Operation(summary = "Ver mi hoja de vida activa")
    public ResponseEntity<Resource> verHojaVida(@AuthenticationPrincipal UsuarioAutenticado actual) {
        var hoja = servicio.hojaVidaPropia(actual.id());
        return DescargaPdf.de(hoja.archivo(), hoja.nombreOriginal());
    }

    @PutMapping("/competencias")
    @Operation(summary = "Guardar mis competencias con su nivel (reemplaza la lista)")
    public PerfilEstudianteDto competencias(@Valid @RequestBody CompetenciasRequest datos,
                                            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.guardarCompetencias(actual.id(), datos);
    }

    @PostMapping("/portafolio")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar proyecto, repositorio o certificación")
    public PortafolioDto agregar(@Valid @RequestBody PortafolioRequest datos,
                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.agregarPortafolio(actual.id(), datos);
    }

    @PutMapping("/portafolio/{id}")
    @Operation(summary = "Editar un elemento del portafolio")
    public PortafolioDto editar(@PathVariable Long id, @Valid @RequestBody PortafolioRequest datos,
                                @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.editarPortafolio(actual.id(), id, datos);
    }

    @DeleteMapping("/portafolio/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un elemento del portafolio")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        servicio.eliminarPortafolio(actual.id(), id);
    }
}
