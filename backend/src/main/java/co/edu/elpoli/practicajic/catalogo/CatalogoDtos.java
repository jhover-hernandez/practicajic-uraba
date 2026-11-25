package co.edu.elpoli.practicajic.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos de entrada y salida del catálogo de áreas y competencias. */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record AreaDto(Long id, String nombre) {
        public static AreaDto de(Area a) {
            return new AreaDto(a.getId(), a.getNombre());
        }
    }

    public record CompetenciaDto(Long id, String nombre, CategoriaCompetencia categoria, String descripcion) {
        public static CompetenciaDto de(Competencia c) {
            return new CompetenciaDto(c.getId(), c.getNombre(), c.getCategoria(), c.getDescripcion());
        }
    }

    public record AreaRequest(
            @NotBlank(message = "Escribe el nombre del área.")
            @Size(max = 80, message = "El nombre no puede superar 80 caracteres.")
            String nombre) {
    }

    public record CompetenciaRequest(
            @NotBlank(message = "Escribe el nombre de la competencia.")
            @Size(max = 80, message = "El nombre no puede superar 80 caracteres.")
            String nombre,

            @NotNull(message = "Selecciona si es técnica o blanda.")
            CategoriaCompetencia categoria,

            @Size(max = 500, message = "La descripción no puede superar 500 caracteres.")
            String descripcion) {
    }
}
