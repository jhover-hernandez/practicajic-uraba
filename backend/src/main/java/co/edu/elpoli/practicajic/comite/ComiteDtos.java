package co.edu.elpoli.practicajic.comite;

import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.OfertaDto;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.PostulanteDto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Matrices de evaluación del comité (RF-29, RF-31), decisiones y actas (RF-33). */
public final class ComiteDtos {

    private ComiteDtos() {
    }

    /** Matriz de evaluación de empresas: 25 + 20 + 20 + 15 + 10 + 10 = 100. */
    public record EvaluarOfertaRequest(
            @NotNull(message = "Califica la pertinencia.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "25", message = "Máximo 25 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal pertinencia,

            @NotNull(message = "Califica la calidad del plan.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "20", message = "Máximo 20 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal calidadPlan,

            @NotNull(message = "Califica las condiciones.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "20", message = "Máximo 20 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal condiciones,

            @NotNull(message = "Califica el incentivo económico.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "15", message = "Máximo 15 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal incentivoEconomico,

            @NotNull(message = "Califica la capacidad de acompañamiento.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "10", message = "Máximo 10 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal acompanamiento,

            @NotNull(message = "Califica la generación de experiencia laboral.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "10", message = "Máximo 10 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal experienciaLaboral,

            @NotNull(message = "Selecciona la decisión.")
            EvaluacionOferta.Decision decision,

            @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.")
            String observaciones) {
    }

    /** Matriz de evaluación del estudiante: 30 + 25 + 15 + 15 + 10 + 5 = 100. */
    public record EvaluarCandidatoRequest(
            @NotNull(message = "Califica las competencias técnicas.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "30", message = "Máximo 30 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal tecnico,

            @NotNull(message = "Califica el ajuste al perfil.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "25", message = "Máximo 25 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal perfil,

            @NotNull(message = "Califica el desempeño académico.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "15", message = "Máximo 15 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal academico,

            @NotNull(message = "Califica el portafolio.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "15", message = "Máximo 15 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal portafolio,

            @NotNull(message = "Califica las competencias blandas.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "10", message = "Máximo 10 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal blandas,

            @NotNull(message = "Califica la entrevista.")
            @DecimalMin(value = "0", message = "Mínimo 0.") @DecimalMax(value = "5", message = "Máximo 5 puntos.")
            @Digits(integer = 2, fraction = 2, message = "Usa máximo dos decimales.")
            BigDecimal entrevista,

            @Size(max = 2000, message = "El concepto no puede superar 2000 caracteres.")
            String concepto,

            boolean recomendado) {
    }

    public record EvaluacionOfertaDto(Long id, Long ofertaId, String oferta, String empresa, String evaluador,
                                      BigDecimal pertinencia, BigDecimal calidadPlan, BigDecimal condiciones,
                                      BigDecimal incentivoEconomico, BigDecimal acompanamiento,
                                      BigDecimal experienciaLaboral, BigDecimal puntajeTotal,
                                      EvaluacionOferta.Decision decision, String observaciones, LocalDateTime fecha) {
        public static EvaluacionOfertaDto de(EvaluacionOferta e) {
            return new EvaluacionOfertaDto(e.getId(), e.getOferta().getId(), e.getOferta().getTitulo(),
                    e.getOferta().getEmpresa().getNombreVisible(), e.getEvaluador().getNombre(), e.getPertinencia(),
                    e.getCalidadPlan(), e.getCondiciones(), e.getIncentivoEconomico(), e.getAcompanamiento(),
                    e.getExperienciaLaboral(), e.getPuntajeTotal(), e.getDecision(), e.getObservaciones(), e.getFecha());
        }
    }

    public record EvaluacionCandidatoDto(Long id, Long postulacionId, String estudiante, String oferta, String empresa,
                                         String evaluador, BigDecimal tecnico, BigDecimal perfil, BigDecimal academico,
                                         BigDecimal portafolio, BigDecimal blandas, BigDecimal entrevista,
                                         BigDecimal puntajeFinal, String concepto, boolean recomendado,
                                         LocalDateTime fecha) {
        public static EvaluacionCandidatoDto de(Evaluacion e) {
            var p = e.getPostulacion();
            return new EvaluacionCandidatoDto(e.getId(), p.getId(), p.getEstudiante().getNombreCompleto(),
                    p.getOferta().getTitulo(), p.getOferta().getEmpresa().getNombreVisible(), e.getEvaluador().getNombre(),
                    e.getPuntajeTecnico(), e.getPuntajePerfil(), e.getPuntajeAcademico(), e.getPuntajePortafolio(),
                    e.getPuntajeBlandas(), e.getPuntajeEntrevista(), e.getPuntajeFinal(), e.getConcepto(),
                    e.isRecomendado(), e.getFecha());
        }
    }

    /** RF-28: oferta pendiente con la empresa que la publica y las evaluaciones anteriores. */
    public record OfertaParaEvaluarDto(OfertaDto oferta, PerfilEmpresaDto empresa,
                                       List<EvaluacionOfertaDto> evaluaciones, BigDecimal puntajeMinimo) {
    }

    public record CandidatoDto(PostulanteDto postulante, String empresa, EvaluacionCandidatoDto miEvaluacion) {
    }

    public record HistorialDto(List<EvaluacionOfertaDto> ofertas, List<EvaluacionCandidatoDto> candidatos) {
    }

    /** RF-33: contenido del acta; el frontend la imprime o la guarda como PDF. */
    public record ActaDto(LocalDate desde, LocalDate hasta, LocalDateTime generada, BigDecimal puntajeMinimo,
                          List<String> evaluadores, List<EvaluacionOfertaDto> ofertas,
                          List<EvaluacionCandidatoDto> candidatos) {
    }
}
