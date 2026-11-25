package co.edu.elpoli.practicajic.practica;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.PerfilEmpresaService;
import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.estudiante.PerfilEstudianteService;
import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.postulacion.EstadoPostulacion;
import co.edu.elpoli.practicajic.postulacion.Postulacion;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import co.edu.elpoli.practicajic.postulacion.PostulacionService;
import co.edu.elpoli.practicajic.practica.PracticaDtos.*;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

/** RF-35 a RF-37: formalización, seguimientos y evaluación final de la práctica. */
@Service
@Transactional
public class PracticaService {

    private final PracticaRepository practicas;
    private final SeguimientoRepository seguimientos;
    private final PostulacionRepository postulaciones;
    private final PostulacionService postulacionService;
    private final PerfilEmpresaService perfilEmpresa;
    private final PerfilEstudianteService perfilEstudiante;
    private final UsuarioRepository usuarios;
    private final ActividadService actividad;
    private final CorreoService correo;

    public PracticaService(PracticaRepository practicas, SeguimientoRepository seguimientos,
                           PostulacionRepository postulaciones, PostulacionService postulacionService,
                           PerfilEmpresaService perfilEmpresa, PerfilEstudianteService perfilEstudiante,
                           UsuarioRepository usuarios, ActividadService actividad, CorreoService correo) {
        this.practicas = practicas;
        this.seguimientos = seguimientos;
        this.postulaciones = postulaciones;
        this.postulacionService = postulacionService;
        this.perfilEmpresa = perfilEmpresa;
        this.perfilEstudiante = perfilEstudiante;
        this.usuarios = usuarios;
        this.actividad = actividad;
        this.correo = correo;
    }

    // ---------- Administrador ----------

    /** Seleccionados que ya aceptaron la práctica y todavía no tienen vinculación formal. */
    @Transactional(readOnly = true)
    public List<PendienteFormalizarDto> pendientesDeFormalizar() {
        return postulaciones.findByEstadoInOrderByFechaAsc(EnumSet.of(EstadoPostulacion.SELECCIONADO)).stream()
                .filter(p -> p.getFechaAceptacion() != null && !practicas.existsByPostulacionId(p.getId()))
                .map(p -> {
                    OfertaPractica o = p.getOferta();
                    return new PendienteFormalizarDto(p.getId(), p.getEstudiante().getNombreCompleto(),
                            p.getEstudiante().getPrograma(), o.getTitulo(), o.getEmpresa().getNombreVisible(),
                            p.getFechaAceptacion(), o.getTipoVinculacion(), o.getIncentivo(), o.getDuracionMeses());
                })
                .toList();
    }

    public PracticaDto formalizar(Long postulacionId, FormalizarRequest d, Long actorId) {
        Postulacion p = postulacionService.buscar(postulacionId);
        if (p.getEstado() != EstadoPostulacion.SELECCIONADO || p.getFechaAceptacion() == null) {
            throw new NegocioException(HttpStatus.CONFLICT,
                    "Solo se formaliza la práctica de un estudiante seleccionado que ya la aceptó.");
        }
        if (practicas.existsByPostulacionId(postulacionId)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Esta práctica ya fue formalizada.");
        }
        if (d.fechaFin() != null && d.fechaFin().isBefore(d.fechaInicio())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la de inicio.");
        }
        Practica practica = new Practica(p);
        practica.setFechaInicio(d.fechaInicio());
        practica.setFechaFin(d.fechaFin());
        practica.setTipoVinculacion(Textos.limpiar(d.tipoVinculacion()));
        practica.setIncentivo(d.incentivo());
        practica.setTutorEmpresa(d.tutorEmpresa().trim());
        practica.setTutorInstitucion(d.tutorInstitucion().trim());
        practica = practicas.save(practica);
        p.setEstado(EstadoPostulacion.PRACTICA_INICIADA);
        actividad.registrar(actorId, "FORMALIZAR_PRACTICA", "practica", practica.getId(), "Postulación " + postulacionId);

        String mensaje = "Se formalizó la práctica \"" + p.getOferta().getTitulo() + "\" de "
                + p.getEstudiante().getNombreCompleto() + " con inicio el " + d.fechaInicio() + ".";
        correo.enviarAviso(p.getEstudiante().getUsuario().getCorreo(), "Práctica formalizada", mensaje);
        correo.enviarAviso(p.getOferta().getEmpresa().getUsuario().getCorreo(), "Práctica formalizada", mensaje);
        return dto(practica);
    }

    @Transactional(readOnly = true)
    public List<PracticaDto> todas(EstadoPractica estado) {
        List<Practica> lista = estado == null ? practicas.findAllByOrderByFechaInicioDesc()
                : practicas.findByEstadoOrderByFechaInicioDesc(estado);
        return lista.stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public PracticaDto detalle(Long id) {
        return dto(buscar(id));
    }

    public PracticaDto registrarComoAdmin(Long practicaId, SeguimientoRequest d, Long actorId) {
        return registrar(buscar(practicaId), d, actorId);
    }

    // ---------- Empresa ----------

    @Transactional(readOnly = true)
    public List<PracticaDto> deEmpresa(Long usuarioId) {
        Empresa e = perfilEmpresa.delUsuario(usuarioId);
        return practicas.findByPostulacionOfertaEmpresaIdOrderByFechaInicioDesc(e.getId()).stream().map(this::dto).toList();
    }

    public PracticaDto registrarComoEmpresa(Long usuarioId, Long practicaId, SeguimientoRequest d) {
        Practica practica = buscar(practicaId);
        Empresa e = perfilEmpresa.delUsuario(usuarioId);
        if (!practica.getPostulacion().getOferta().getEmpresa().getId().equals(e.getId())) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "La práctica no existe.");
        }
        return registrar(practica, d, usuarioId);
    }

    // ---------- Estudiante ----------

    @Transactional(readOnly = true)
    public List<PracticaDto> delEstudiante(Long usuarioId) {
        Estudiante e = perfilEstudiante.delUsuario(usuarioId);
        return practicas.findByPostulacionEstudianteIdOrderByFechaInicioDesc(e.getId()).stream().map(this::dto).toList();
    }

    // ---------- Utilidades ----------

    /** RF-36 y RF-37. La evaluación final cierra la práctica y exige calificación. */
    private PracticaDto registrar(Practica practica, SeguimientoRequest d, Long actorId) {
        if (practica.getEstado() != EstadoPractica.INICIADA) {
            throw new NegocioException(HttpStatus.CONFLICT, "La práctica ya terminó; no se pueden agregar seguimientos.");
        }
        if (d.fecha().isBefore(practica.getFechaInicio())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "La fecha no puede ser anterior al inicio de la práctica.");
        }
        boolean esFinal = d.tipo() == Seguimiento.Tipo.EVALUACION_FINAL;
        boolean conNota = esFinal || d.tipo() == Seguimiento.Tipo.EVALUACION_PARCIAL;
        if (conNota && d.calificacion() == null) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "La evaluación necesita una calificación de 0 a 5.");
        }
        Usuario responsable = usuarios.findById(actorId)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Tu sesión expiró. Inicia sesión de nuevo."));
        Seguimiento s = seguimientos.save(new Seguimiento(practica, responsable, d.fecha(), d.tipo(),
                d.descripcion().trim(), d.calificacion()));
        actividad.registrar(actorId, esFinal ? "EVALUACION_FINAL" : "REGISTRAR_SEGUIMIENTO", "practica",
                practica.getId(), d.tipo() + (d.calificacion() != null ? " · " + d.calificacion() : ""));

        Postulacion p = practica.getPostulacion();
        if (esFinal) {
            practica.setEstado(EstadoPractica.FINALIZADA);
            if (practica.getFechaFin() == null || practica.getFechaFin().isAfter(s.getFecha())) {
                practica.setFechaFin(s.getFecha());
            }
            p.setEstado(EstadoPostulacion.PRACTICA_FINALIZADA);
            correo.enviarAviso(p.getEstudiante().getUsuario().getCorreo(), "Práctica finalizada",
                    "Se registró la evaluación final de tu práctica \"" + p.getOferta().getTitulo()
                            + "\" con calificación " + d.calificacion() + ".");
        }
        return dto(practica);
    }

    private Practica buscar(Long id) {
        return practicas.findById(id).orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La práctica no existe."));
    }

    private PracticaDto dto(Practica pr) {
        Postulacion p = pr.getPostulacion();
        Estudiante e = p.getEstudiante();
        OfertaPractica o = p.getOferta();
        List<SeguimientoDto> lista = seguimientos.findByPracticaIdOrderByFechaAscIdAsc(pr.getId()).stream()
                .map(SeguimientoDto::de).toList();
        BigDecimal notaFinal = lista.stream().filter(s -> s.tipo() == Seguimiento.Tipo.EVALUACION_FINAL)
                .map(SeguimientoDto::calificacion).findFirst().orElse(null);
        return new PracticaDto(pr.getId(), p.getId(), e.getId(), e.getNombreCompleto(), e.getUsuario().getCorreo(),
                e.getPrograma(), o.getTitulo(), o.getEmpresa().getNombreVisible(), o.getArea().getNombre(),
                pr.getFechaInicio(), pr.getFechaFin(), pr.getTipoVinculacion(), pr.getIncentivo(), pr.getTutorEmpresa(),
                pr.getTutorInstitucion(), pr.getEstado(), lista, notaFinal);
    }
}
