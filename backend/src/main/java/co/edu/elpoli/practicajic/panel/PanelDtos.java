package co.edu.elpoli.practicajic.panel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PanelDtos {

    private PanelDtos() {
    }

    public record Conteo(String etiqueta, long cantidad) {
    }

    /** RF-38 y RF-39: indicadores del panel administrativo. */
    public record IndicadoresDto(String periodo, LocalDate desde, LocalDate hasta, List<String> periodos,
                                 long empresasValidadas, long estudiantesValidados, long convocatoriasAbiertas,
                                 long postulaciones, long practicas, long practicasFinalizadas,
                                 BigDecimal tasaColocacion, long estudiantesPostulados, long estudiantesConPractica,
                                 long empresasPendientes, long estudiantesPendientes, long ofertasEnRevision,
                                 long ofertasSinEvaluador, long practicasPorFormalizar,
                                 List<Conteo> empresasPorMunicipio, List<Conteo> ofertasPorArea,
                                 List<Conteo> postulacionesPorEstado) {
    }
}
