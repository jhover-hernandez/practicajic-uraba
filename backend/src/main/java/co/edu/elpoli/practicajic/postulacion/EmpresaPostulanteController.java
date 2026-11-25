package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.archivo.DescargaPdf;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.*;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresa")
@Tag(name = "Empresa · Postulantes", description = "RF-10, RF-24, RF-26 y RNF-07")
public class EmpresaPostulanteController {

    private final PostulacionService servicio;

    public EmpresaPostulanteController(PostulacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/ofertas/{ofertaId}/postulantes")
    @Operation(summary = "Postulantes de una de mis ofertas, ordenados por competencias coincidentes")
    public List<PostulanteDto> postulantes(@PathVariable Long ofertaId, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.postulantesDeOferta(actual.id(), ofertaId);
    }

    @PatchMapping("/postulaciones/{id}/estado")
    @Operation(summary = "Avanzar la postulación al siguiente estado o marcarla como no seleccionada")
    public PostulanteDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest datos,
                                       @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.cambiarEstadoPorEmpresa(actual.id(), id, datos);
    }

    @PutMapping("/postulaciones/{id}/entrevista")
    @Operation(summary = "Programar o reprogramar la entrevista")
    public PostulanteDto entrevista(@PathVariable Long id, @Valid @RequestBody EntrevistaRequest datos,
                                    @AuthenticationPrincipal UsuarioAutenticado actual) {
        return servicio.programarEntrevista(actual.id(), id, datos);
    }

    @GetMapping("/postulaciones/{id}/hoja-vida")
    @Operation(summary = "Ver la hoja de vida de un postulante a mis ofertas")
    public ResponseEntity<Resource> hojaVida(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        var hoja = servicio.hojaVidaParaEmpresa(actual.id(), id);
        return DescargaPdf.de(hoja.archivo(), hoja.nombreOriginal());
    }
}
