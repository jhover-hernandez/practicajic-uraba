package co.edu.elpoli.practicajic.panel;

import co.edu.elpoli.practicajic.panel.PanelDtos.IndicadoresDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración · Panel e informes", description = "RF-38 a RF-40")
public class PanelController {

    private final PanelService panel;
    private final InformeService informes;

    public PanelController(PanelService panel, InformeService informes) {
        this.panel = panel;
        this.informes = informes;
    }

    @GetMapping("/panel")
    @Operation(summary = "Indicadores del panel, opcionalmente por semestre (por ejemplo 2026-2)")
    public IndicadoresDto indicadores(@RequestParam(required = false) String periodo) {
        return panel.indicadores(periodo);
    }

    @GetMapping("/informes/{tipo}")
    @Operation(summary = "Descargar un informe en formato Excel (CSV): empresas, estudiantes, convocatorias, postulaciones o practicas")
    public ResponseEntity<byte[]> informe(@PathVariable String tipo, @RequestParam(required = false) String periodo) {
        byte[] contenido = informes.csv(tipo, periodo);
        String nombre = "informe-" + tipo + "-" + (periodo == null || periodo.isBlank() ? LocalDate.now() : periodo) + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
