package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.oferta.ConvocatoriaService;
import co.edu.elpoli.practicajic.oferta.Modalidad;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.ConvocatoriaDto;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.MiPostulacionDto;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@Tag(name = "Estudiante · Convocatorias y postulaciones", description = "RF-17, RF-18, RF-22, RF-23, RF-25 y RF-27")
public class EstudiantePostulacionController {

    private final ConvocatoriaService convocatorias;
    private final PostulacionService postulaciones;

    public EstudiantePostulacionController(ConvocatoriaService convocatorias, PostulacionService postulaciones) {
        this.convocatorias = convocatorias;
        this.postulaciones = postulaciones;
    }

    @GetMapping("/convocatorias")
    @Operation(summary = "Buscar convocatorias abiertas por texto, área, municipio y modalidad")
    public List<ConvocatoriaDto> buscar(@RequestParam(required = false, name = "q") String texto,
                                        @RequestParam(required = false) Long areaId,
                                        @RequestParam(required = false) String municipio,
                                        @RequestParam(required = false) Modalidad modalidad,
                                        @AuthenticationPrincipal UsuarioAutenticado actual) {
        return convocatorias.buscar(actual.id(), texto, areaId, municipio, modalidad);
    }

    @GetMapping("/convocatorias/{id}")
    @Operation(summary = "Ver el detalle de una convocatoria y de la empresa")
    public ConvocatoriaDto detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return convocatorias.detalle(actual.id(), id);
    }

    @PostMapping("/convocatorias/{id}/postular")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Postularme a una convocatoria abierta")
    public MiPostulacionDto postular(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return postulaciones.postular(actual.id(), id);
    }

    @GetMapping("/postulaciones")
    @Operation(summary = "Consultar el estado de mis postulaciones")
    public List<MiPostulacionDto> mias(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return postulaciones.mias(actual.id());
    }

    @PostMapping("/postulaciones/{id}/aceptar")
    @Operation(summary = "Aceptar la práctica en la que fui seleccionado")
    public MiPostulacionDto aceptar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return postulaciones.aceptar(actual.id(), id);
    }
}
