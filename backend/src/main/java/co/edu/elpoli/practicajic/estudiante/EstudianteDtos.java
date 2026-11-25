package co.edu.elpoli.practicajic.estudiante;

import co.edu.elpoli.practicajic.catalogo.CategoriaCompetencia;
import co.edu.elpoli.practicajic.catalogo.Nivel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** Datos del perfil digital del estudiante (RF-12 a RF-15). */
public final class EstudianteDtos {

    private EstudianteDtos() {
    }

    public record PerfilEstudianteDto(Long id, String correo, String documento, String nombres, String apellidos,
                                      String programa, Short semestre, String municipio, String disponibilidad,
                                      EstadoEstudiante estado, String motivoRechazo, HojaVidaDto hojaVida,
                                      List<CompetenciaEstudianteDto> competencias, List<PortafolioDto> portafolio,
                                      boolean puedePostularse) {
    }

    public record HojaVidaDto(Long id, String nombreOriginal, int version, LocalDateTime fechaActualizacion,
                              Long tamanoBytes) {
        public static HojaVidaDto de(HojaVida h) {
            return new HojaVidaDto(h.getId(), h.getNombreOriginal(), h.getVersion(), h.getFechaActualizacion(),
                    h.getTamanoBytes());
        }
    }

    public record CompetenciaEstudianteDto(Long competenciaId, String nombre, CategoriaCompetencia categoria,
                                           Nivel nivel) {
        public static CompetenciaEstudianteDto de(EstudianteCompetencia ec) {
            return new CompetenciaEstudianteDto(ec.getCompetencia().getId(), ec.getCompetencia().getNombre(),
                    ec.getCompetencia().getCategoria(), ec.getNivel());
        }
    }

    public record PortafolioDto(Long id, String tipo, String titulo, String url, String descripcion) {
        public static PortafolioDto de(Portafolio p) {
            return new PortafolioDto(p.getId(), p.getTipo(), p.getTitulo(), p.getUrl(), p.getDescripcion());
        }
    }

    public record ActualizarPerfilRequest(
            @NotBlank(message = "Escribe tus nombres.")
            @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres.")
            String nombres,

            @NotBlank(message = "Escribe tus apellidos.")
            @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres.")
            String apellidos,

            @NotBlank(message = "Escribe tu programa académico.")
            @Size(max = 120, message = "El programa no puede superar 120 caracteres.")
            String programa,

            @NotNull(message = "Selecciona tu semestre.")
            @Min(value = 1, message = "El semestre debe estar entre 1 y 12.")
            @Max(value = 12, message = "El semestre debe estar entre 1 y 12.")
            Short semestre,

            @Size(max = 60, message = "El municipio no puede superar 60 caracteres.")
            String municipio,

            @Size(max = 60, message = "La disponibilidad no puede superar 60 caracteres.")
            String disponibilidad) {
    }

    public record CompetenciaItem(
            @NotNull(message = "Selecciona la competencia.")
            Long competenciaId,

            @NotNull(message = "Selecciona el nivel.")
            Nivel nivel) {
    }

    public record CompetenciasRequest(
            @NotNull(message = "Envía la lista de competencias.")
            @Size(max = 40, message = "Puedes registrar máximo 40 competencias.")
            List<@Valid CompetenciaItem> competencias) {
    }

    public record PortafolioRequest(
            @NotBlank(message = "Selecciona el tipo.")
            @Pattern(regexp = "PROYECTO|REPOSITORIO|CERTIFICACION", message = "El tipo no es válido.")
            String tipo,

            @NotBlank(message = "Escribe el título.")
            @Size(max = 150, message = "El título no puede superar 150 caracteres.")
            String titulo,

            @Size(max = 255, message = "El enlace no puede superar 255 caracteres.")
            @Pattern(regexp = "^$|^https?://\\S+$", message = "El enlace debe empezar por http:// o https://")
            String url,

            @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres.")
            String descripcion) {
    }
}
