package co.edu.elpoli.practicajic.validacion;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import co.edu.elpoli.practicajic.empresa.EmpresaRepository;
import co.edu.elpoli.practicajic.empresa.EstadoValidacionEmpresa;
import co.edu.elpoli.practicajic.estudiante.*;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.PerfilEstudianteDto;
import co.edu.elpoli.practicajic.validacion.ValidacionDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-07 y RF-16: el administrador valida, rechaza o inactiva empresas y estudiantes. */
@Service
@Transactional
public class ValidacionService {

    private final EmpresaRepository empresas;
    private final EstudianteRepository estudiantes;
    private final HojaVidaRepository hojasVida;
    private final PerfilEstudianteService perfilEstudiante;
    private final ActividadService actividad;
    private final CorreoService correo;

    public ValidacionService(EmpresaRepository empresas, EstudianteRepository estudiantes, HojaVidaRepository hojasVida,
                             PerfilEstudianteService perfilEstudiante, ActividadService actividad,
                             CorreoService correo) {
        this.empresas = empresas;
        this.estudiantes = estudiantes;
        this.hojasVida = hojasVida;
        this.perfilEstudiante = perfilEstudiante;
        this.actividad = actividad;
        this.correo = correo;
    }

    @Transactional(readOnly = true)
    public List<PerfilEmpresaDto> empresas(EstadoValidacionEmpresa estado) {
        List<Empresa> lista = estado == null ? empresas.findAllByOrderByRazonSocialAsc()
                : empresas.findByEstadoValidacionOrderByIdAsc(estado);
        return lista.stream().map(PerfilEmpresaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<EstudianteResumenDto> estudiantes(EstadoEstudiante estado) {
        List<Estudiante> lista = estado == null ? estudiantes.findAllByOrderByApellidosAscNombresAsc()
                : estudiantes.findByEstadoOrderByIdAsc(estado);
        return lista.stream().map(this::resumen).toList();
    }

    @Transactional(readOnly = true)
    public PerfilEstudianteDto estudiante(Long id) {
        return perfilEstudiante.dto(buscarEstudiante(id));
    }

    @Transactional(readOnly = true)
    public PerfilEstudianteService.HojaVidaArchivo hojaVidaEstudiante(Long id) {
        return perfilEstudiante.hojaVidaDe(buscarEstudiante(id).getId());
    }

    public PerfilEmpresaDto decidirEmpresa(Long id, DecisionRequest datos, Long actorId) {
        Empresa e = empresas.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La empresa no existe."));
        String motivo = motivoSiHaceFalta(datos);
        EstadoValidacionEmpresa nuevo = switch (datos.decision()) {
            case VALIDAR -> EstadoValidacionEmpresa.VALIDADA;
            case RECHAZAR -> EstadoValidacionEmpresa.RECHAZADA;
            case INACTIVAR -> EstadoValidacionEmpresa.INACTIVA;
        };
        e.setEstadoValidacion(nuevo);
        e.setMotivoRechazo(nuevo == EstadoValidacionEmpresa.VALIDADA ? null : motivo);
        actividad.registrar(actorId, datos.decision() + "_EMPRESA", "empresa", id, motivo);
        correo.enviarAviso(e.getUsuario().getCorreo(), "Validación de la empresa", switch (nuevo) {
            case VALIDADA -> "El Politécnico validó a " + e.getRazonSocial() + ". Ya puedes publicar necesidades de práctica.";
            case RECHAZADA -> "El registro de " + e.getRazonSocial() + " fue rechazado. Motivo: " + motivo
                    + "\nPuedes corregir los datos desde el perfil de la empresa y se revisarán de nuevo.";
            default -> "El perfil de " + e.getRazonSocial() + " quedó inactivo. Motivo: " + motivo;
        });
        return PerfilEmpresaDto.de(e);
    }

    public EstudianteResumenDto decidirEstudiante(Long id, DecisionRequest datos, Long actorId) {
        Estudiante e = buscarEstudiante(id);
        String motivo = motivoSiHaceFalta(datos);
        EstadoEstudiante nuevo = switch (datos.decision()) {
            case VALIDAR -> EstadoEstudiante.VALIDADO;
            case RECHAZAR -> EstadoEstudiante.RECHAZADO;
            case INACTIVAR -> EstadoEstudiante.INACTIVO;
        };
        e.setEstado(nuevo);
        e.setMotivoRechazo(nuevo == EstadoEstudiante.VALIDADO ? null : motivo);
        actividad.registrar(actorId, datos.decision() + "_ESTUDIANTE", "estudiante", id, motivo);
        correo.enviarAviso(e.getUsuario().getCorreo(), "Validación de tu perfil", switch (nuevo) {
            case VALIDADO -> "El Politécnico validó tu perfil. Si ya cargaste tu hoja de vida, puedes postularte a las convocatorias.";
            case RECHAZADO -> "Tu registro fue rechazado. Motivo: " + motivo
                    + "\nPuedes corregir tus datos en Mi perfil digital y se revisarán de nuevo.";
            default -> "Tu perfil quedó inactivo. Motivo: " + motivo;
        });
        return resumen(e);
    }

    private static String motivoSiHaceFalta(DecisionRequest datos) {
        String motivo = Textos.limpiar(datos.motivo());
        if (datos.decision() != Decision.VALIDAR && motivo == null) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Escribe el motivo para que el usuario sepa qué corregir.");
        }
        return motivo;
    }

    private Estudiante buscarEstudiante(Long id) {
        return estudiantes.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El estudiante no existe."));
    }

    private EstudianteResumenDto resumen(Estudiante e) {
        return new EstudianteResumenDto(e.getId(), e.getNombreCompleto(), e.getDocumento(), e.getUsuario().getCorreo(),
                e.getPrograma(), e.getSemestre(), e.getMunicipio(), e.getEstado(), e.getMotivoRechazo(),
                e.getUsuario().getFechaCreacion(), hojasVida.existsByEstudianteIdAndActivaTrue(e.getId()));
    }
}
