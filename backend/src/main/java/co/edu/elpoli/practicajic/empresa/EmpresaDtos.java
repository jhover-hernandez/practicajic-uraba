package co.edu.elpoli.practicajic.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Datos del perfil de la empresa (RF-06). */
public final class EmpresaDtos {

    private EmpresaDtos() {
    }

    public record PerfilEmpresaDto(Long id, String correo, String nit, String razonSocial, String nombreComercial,
                                   String sector, String tamano, String municipio, String contacto,
                                   EstadoValidacionEmpresa estadoValidacion, String motivoRechazo,
                                   LocalDateTime fechaRegistro) {
        public static PerfilEmpresaDto de(Empresa e) {
            return new PerfilEmpresaDto(e.getId(), e.getUsuario().getCorreo(), e.getNit(), e.getRazonSocial(),
                    e.getNombreComercial(), e.getSector(), e.getTamano(), e.getMunicipio(), e.getContacto(),
                    e.getEstadoValidacion(), e.getMotivoRechazo(), e.getUsuario().getFechaCreacion());
        }
    }

    public record ActualizarEmpresaRequest(
            @NotBlank(message = "Escribe la razón social.")
            @Size(max = 150, message = "La razón social no puede superar 150 caracteres.")
            String razonSocial,

            @Size(max = 150, message = "El nombre comercial no puede superar 150 caracteres.")
            String nombreComercial,

            @Size(max = 80, message = "El sector no puede superar 80 caracteres.")
            String sector,

            @Pattern(regexp = "^$|MICRO|PEQUENA|MEDIANA|GRANDE", message = "Selecciona un tamaño válido.")
            String tamano,

            @NotBlank(message = "Selecciona el municipio.")
            @Size(max = 60, message = "El municipio no puede superar 60 caracteres.")
            String municipio,

            @Size(max = 150, message = "El contacto no puede superar 150 caracteres.")
            String contacto) {
    }
}
