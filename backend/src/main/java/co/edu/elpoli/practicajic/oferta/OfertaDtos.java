package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.catalogo.CategoriaCompetencia;
import co.edu.elpoli.practicajic.catalogo.Nivel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Datos de ofertas y convocatorias (RF-08, RF-09, RF-17, RF-18). */
public final class OfertaDtos {

    private OfertaDtos() {
    }

    public record CompetenciaRequerida(
            @NotNull(message = "Selecciona la competencia.")
            Long competenciaId,

            @NotNull(message = "Selecciona el nivel requerido.")
            Nivel nivelRequerido) {
    }

    public record OfertaRequest(
            @NotNull(message = "Selecciona el área.")
            Long areaId,

            @NotBlank(message = "Escribe el título o cargo.")
            @Size(max = 150, message = "El título no puede superar 150 caracteres.")
            String titulo,

            @NotBlank(message = "Describe la necesidad.")
            @Size(max = 4000, message = "La descripción no puede superar 4000 caracteres.")
            String descripcion,

            @Size(max = 4000, message = "El perfil no puede superar 4000 caracteres.")
            String perfil,

            @NotBlank(message = "Escribe las actividades que realizará el practicante.")
            @Size(max = 4000, message = "Las actividades no pueden superar 4000 caracteres.")
            String actividades,

            @NotNull(message = "Agrega al menos una competencia.")
            @Size(min = 1, max = 20, message = "Agrega entre 1 y 20 competencias.")
            List<@Valid CompetenciaRequerida> competencias,

            @NotNull(message = "Selecciona la modalidad.")
            Modalidad modalidad,

            @NotBlank(message = "Selecciona el municipio.")
            @Size(max = 60, message = "El municipio no puede superar 60 caracteres.")
            String municipio,

            @NotNull(message = "Escribe la duración en meses.")
            @Min(value = 1, message = "La duración debe estar entre 1 y 24 meses.")
            @Max(value = 24, message = "La duración debe estar entre 1 y 24 meses.")
            Short duracionMeses,

            @NotNull(message = "Escribe el número de vacantes.")
            @Min(value = 1, message = "Las vacantes deben estar entre 1 y 50.")
            @Max(value = 50, message = "Las vacantes deben estar entre 1 y 50.")
            Short vacantes,

            @Size(max = 100, message = "El horario no puede superar 100 caracteres.")
            String horario,

            @Size(max = 60, message = "El tipo de vinculación no puede superar 60 caracteres.")
            String tipoVinculacion,

            @DecimalMin(value = "0", message = "El incentivo no puede ser negativo.")
            @Digits(integer = 10, fraction = 2, message = "El incentivo no tiene un formato válido.")
            BigDecimal incentivo,

            @NotNull(message = "Selecciona la fecha de cierre.")
            @FutureOrPresent(message = "La fecha de cierre no puede estar en el pasado.")
            LocalDate fechaCierre,

            /** true: se envía al comité de una vez; false: queda como borrador. */
            boolean enviarARevision) {
    }

    public record CompetenciaOfertaDto(Long competenciaId, String nombre, CategoriaCompetencia categoria,
                                       Nivel nivelRequerido) {
        public static CompetenciaOfertaDto de(OfertaCompetencia oc) {
            return new CompetenciaOfertaDto(oc.getCompetencia().getId(), oc.getCompetencia().getNombre(),
                    oc.getCompetencia().getCategoria(), oc.getNivelRequerido());
        }
    }

    public record OfertaDto(Long id, String titulo, Long empresaId, String empresa, Long areaId, String area,
                            String descripcion, String perfil, String actividades, Modalidad modalidad,
                            String municipio, Short duracionMeses, Short vacantes, String horario,
                            String tipoVinculacion, BigDecimal incentivo, LocalDate fechaPublicacion,
                            LocalDate fechaCierre, EstadoOferta estado, boolean abierta, String observacionesComite,
                            LocalDateTime fechaCreacion, List<CompetenciaOfertaDto> competencias,
                            long postulados, long vacantesCubiertas, Long evaluadorId, String evaluador) {
    }

    /** RF-18: datos de la empresa que ve el estudiante en el detalle de la convocatoria. */
    public record EmpresaPublicaDto(String nombre, String razonSocial, String sector, String tamano, String municipio) {
    }

    /** RF-17 y RF-25: convocatoria vista por un estudiante, con sus competencias coincidentes. */
    public record ConvocatoriaDto(OfertaDto oferta, EmpresaPublicaDto empresa, int coincidentes,
                                  List<Long> competenciasCoincidentes, boolean yaPostulado, boolean vacantesDisponibles) {
    }

    public record AsignarEvaluadorRequest(
            @NotNull(message = "Selecciona el evaluador.")
            Long evaluadorId) {
    }
}
