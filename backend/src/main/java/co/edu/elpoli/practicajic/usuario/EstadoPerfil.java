package co.edu.elpoli.practicajic.usuario;

import co.edu.elpoli.practicajic.auth.dto.UsuarioResponse;
import co.edu.elpoli.practicajic.empresa.EmpresaRepository;
import co.edu.elpoli.practicajic.estudiante.EstudianteRepository;
import org.springframework.stereotype.Component;

/** Une el usuario con el estado de validación de su perfil de estudiante o empresa. */
@Component
public class EstadoPerfil {

    private final EstudianteRepository estudiantes;
    private final EmpresaRepository empresas;

    public EstadoPerfil(EstudianteRepository estudiantes, EmpresaRepository empresas) {
        this.estudiantes = estudiantes;
        this.empresas = empresas;
    }

    public String de(Usuario usuario) {
        return switch (usuario.getRol()) {
            case ESTUDIANTE -> estudiantes.findByUsuarioId(usuario.getId())
                    .map(e -> e.getEstado().name()).orElse(null);
            case EMPRESA -> empresas.findByUsuarioId(usuario.getId())
                    .map(e -> e.getEstadoValidacion().name()).orElse(null);
            case ADMIN, COMITE -> null;
        };
    }

    public UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getCorreo(), usuario.getNombre(), usuario.getRol(), de(usuario));
    }
}
