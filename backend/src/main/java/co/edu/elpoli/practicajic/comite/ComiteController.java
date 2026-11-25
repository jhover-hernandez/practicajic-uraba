package co.edu.elpoli.practicajic.comite;

import co.edu.elpoli.practicajic.archivo.DescargaPdf;
import co.edu.elpoli.practicajic.comite.ComiteDtos.*;
import co.edu.elpoli.practicajic.estudiante.PerfilEstudianteService;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.CambioEstadoRequest;
import co.edu.elpoli.practicajic.postulacion.PostulacionService;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Ruta bajo /api/comite: solo accesible con rol COMITE. */
@RestController
@RequestMapping("/api/comite")
@Tag(name = "Comité evaluador", description = "RF-28 a RF-33")
public class ComiteController {

    private final ComiteService servicio;
    private final PostulacionService postulaciones;
    private final PerfilEstudianteService perfilEstudiante;

    public ComiteController(ComiteService servicio, PostulacionService postulaciones,
                            PerfilEstudianteService perfilEstudiante) {
        this.servicio = servicio;
        this.postulaciones = postulaciones;
        this.perfilEstudiante = perfilEstudiante;
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Ofertas pendientes de evaluación (asignadas a mí o sin evaluador)")
    public List<OfertaParaEvaluarDto> pendientes(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.ofertasPendientes(actual.id());
    }

    @GetMapping("/ofertas/{id}")
    @Operation(summary = "Detalle de una oferta con la empresa y las evaluaciones anteriores")
    public OfertaParaEvaluarDto oferta(@PathVariable Long id) {
        return servicio.oferta(id);
    }

    @PostMapping("/ofertas/{id}/evaluacion")
    @Operation(summary = "Calificar la oferta con la matriz de 100 puntos y decidir")
    public OfertaParaEvaluarDto evaluarOferta(@PathVariable Long id, @Valid @RequestBody EvaluarOfertaRequest datos,
                                              @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.evaluarOferta(actual.id(), id, datos);
    }

    @GetMapping("/candidatos")
    @Operation(summary = "Candidatos por calificar (asignados a mí o sin evaluador)")
    public List<CandidatoDto> candidatos(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.candidatos(actual.id());
    }

    @PutMapping("/candidatos/{postulacionId}/evaluacion")
    @Operation(summary = "Calificar al candidato con la matriz del estudiante, emitir concepto y recomendar")
    public CandidatoDto evaluarCandidato(@PathVariable Long postulacionId,
                                         @Valid @RequestBody EvaluarCandidatoRequest datos,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.evaluarCandidato(actual.id(), postulacionId, datos);
    }

    @PatchMapping("/candidatos/{postulacionId}/estado")
    @Operation(summary = "Cambiar el estado de la postulación según el flujo")
    public CandidatoDto cambiarEstado(@PathVariable Long postulacionId, @Valid @RequestBody CambioEstadoRequest datos,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.cambiarEstado(actual.id(), postulacionId, datos);
    }

    @GetMapping("/candidatos/{postulacionId}/hoja-vida")
    @Operation(summary = "Ver la hoja de vida del candidato")
    public ResponseEntity<Resource> hojaVida(@PathVariable Long postulacionId) {
        var p = postulaciones.buscar(postulacionId);
        var hoja = perfilEstudiante.hojaVidaDe(p.getEstudiante().getId());
        return DescargaPdf.de(hoja.archivo(), hoja.nombreOriginal());
    }

    @GetMapping("/historial")
    @Operation(summary = "Mis evaluaciones de ofertas y candidatos")
    public HistorialDto historial(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.historial(actual.id());
    }

    @GetMapping("/acta")
    @Operation(summary = "Contenido del acta de comité de un periodo")
    public ActaDto acta(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.acta(desde, hasta);
    }
}
