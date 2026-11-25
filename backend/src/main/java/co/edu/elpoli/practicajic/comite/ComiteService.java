package co.edu.elpoli.practicajic.comite;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.comite.ComiteDtos.*;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import co.edu.elpoli.practicajic.oferta.EstadoOferta;
import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.oferta.OfertaRepository;
import co.edu.elpoli.practicajic.oferta.OfertaService;
import co.edu.elpoli.practicajic.postulacion.EstadoPostulacion;
import co.edu.elpoli.practicajic.postulacion.Postulacion;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.CambioEstadoRequest;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.PostulanteDto;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import co.edu.elpoli.practicajic.postulacion.PostulacionService;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/** RF-28 a RF-33: el comité revisa y califica ofertas y candidatos, decide y genera actas. */
@Service
@Transactional
public class ComiteService {

    /** Candidatos que el comité puede calificar: el proceso sigue abierto o acaban de ser seleccionados. */
    private static final Set<EstadoPostulacion> CALIFICABLES = EnumSet.of(EstadoPostulacion.EN_REVISION,
            EstadoPostulacion.PRESELECCIONADO, EstadoPostulacion.ENTREVISTA, EstadoPostulacion.SELECCIONADO);

    private final OfertaRepository ofertas;
    private final OfertaService ofertaService;
    private final EvaluacionOfertaRepository evaluacionesOferta;
    private final EvaluacionRepository evaluaciones;
    private final PostulacionRepository postulaciones;
    private final PostulacionService postulacionService;
    private final UsuarioRepository usuarios;
    private final ActividadService actividad;
    private final CorreoService correo;
    private final BigDecimal puntajeMinimo;

    public ComiteService(OfertaRepository ofertas, OfertaService ofertaService,
                         EvaluacionOfertaRepository evaluacionesOferta, EvaluacionRepository evaluaciones,
                         PostulacionRepository postulaciones, PostulacionService postulacionService,
                         UsuarioRepository usuarios, ActividadService actividad, CorreoService correo,
                         @Value("${app.comite.puntaje-minimo}") BigDecimal puntajeMinimo) {
        this.ofertas = ofertas;
        this.ofertaService = ofertaService;
        this.evaluacionesOferta = evaluacionesOferta;
        this.evaluaciones = evaluaciones;
        this.postulaciones = postulaciones;
        this.postulacionService = postulacionService;
        this.usuarios = usuarios;
        this.actividad = actividad;
        this.correo = correo;
        this.puntajeMinimo = puntajeMinimo;
    }

    // ---------- Ofertas (RF-28 a RF-30) ----------

    /** Ofertas en revisión asignadas a este evaluador o todavía sin evaluador. */
    @Transactional(readOnly = true)
    public List<OfertaParaEvaluarDto> ofertasPendientes(Long evaluadorId) {
        return ofertas.findByEstadoOrderByFechaCreacionAsc(EstadoOferta.EN_REVISION).stream()
                .filter(o -> o.getEvaluador() == null || o.getEvaluador().getId().equals(evaluadorId))
                .map(this::paraEvaluar)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfertaParaEvaluarDto oferta(Long id) {
        return paraEvaluar(ofertaService.buscar(id));
    }

    public OfertaParaEvaluarDto evaluarOferta(Long evaluadorId, Long ofertaId, EvaluarOfertaRequest d) {
        OfertaPractica oferta = ofertaService.buscar(ofertaId);
        if (oferta.getEstado() != EstadoOferta.EN_REVISION) {
            throw new NegocioException(HttpStatus.CONFLICT, "Esta oferta no está pendiente de evaluación.");
        }
        Usuario evaluador = permitido(evaluadorId, oferta.getEvaluador());
        String observaciones = Textos.limpiar(d.observaciones());
        EvaluacionOferta ev = new EvaluacionOferta(oferta, evaluador, d.pertinencia(), d.calidadPlan(), d.condiciones(),
                d.incentivoEconomico(), d.acompanamiento(), d.experienciaLaboral(), d.decision(), observaciones);

        // RN-02: solo se aprueba con el puntaje mínimo definido por el Politécnico.
        if (d.decision() == EvaluacionOferta.Decision.APROBAR && ev.getPuntajeTotal().compareTo(puntajeMinimo) < 0) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "El puntaje total (" + ev.getPuntajeTotal()
                    + ") no alcanza el mínimo de aprobación (" + puntajeMinimo + "). Solicita ajustes o rechaza la oferta.");
        }
        if (d.decision() != EvaluacionOferta.Decision.APROBAR && observaciones == null) {
            throw new NegocioException(HttpStatus.BAD_REQUEST,
                    "Escribe las observaciones para que la empresa sepa qué ajustar o por qué se rechazó.");
        }
        evaluacionesOferta.save(ev);

        oferta.setObservacionesComite(observaciones);
        if (oferta.getEvaluador() == null) {
            oferta.setEvaluador(evaluador);
        }
        String mensaje = switch (d.decision()) {
            case APROBAR -> {
                oferta.setEstado(EstadoOferta.APROBADA);
                oferta.setFechaPublicacion(LocalDate.now());
                yield "El comité aprobó la oferta \"" + oferta.getTitulo() + "\" con " + ev.getPuntajeTotal()
                        + " puntos. Ya está publicada como convocatoria abierta.";
            }
            case AJUSTES -> {
                oferta.setEstado(EstadoOferta.AJUSTES_SOLICITADOS);
                yield "El comité solicitó ajustes a la oferta \"" + oferta.getTitulo() + "\": " + observaciones;
            }
            case RECHAZAR -> {
                oferta.setEstado(EstadoOferta.RECHAZADA);
                yield "El comité rechazó la oferta \"" + oferta.getTitulo() + "\". Motivo: " + observaciones;
            }
        };
        actividad.registrar(evaluadorId, "EVALUAR_OFERTA", "oferta_practica", ofertaId,
                d.decision() + " · " + ev.getPuntajeTotal() + " puntos");
        correo.enviarAviso(oferta.getEmpresa().getUsuario().getCorreo(), "Decisión del comité", mensaje);
        return paraEvaluar(oferta);
    }

    // ---------- Candidatos (RF-31, RF-32) ----------

    @Transactional(readOnly = true)
    public List<CandidatoDto> candidatos(Long evaluadorId) {
        return postulaciones.findByEstadoInOrderByFechaAsc(CALIFICABLES).stream()
                .filter(p -> p.getEvaluador() == null || p.getEvaluador().getId().equals(evaluadorId))
                .map(p -> candidato(p, evaluadorId))
                .toList();
    }

    public CandidatoDto evaluarCandidato(Long evaluadorId, Long postulacionId, EvaluarCandidatoRequest d) {
        Postulacion p = postulacionService.buscar(postulacionId);
        if (!CALIFICABLES.contains(p.getEstado())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Este candidato no está en una etapa que se pueda calificar.");
        }
        Usuario evaluador = permitido(evaluadorId, p.getEvaluador());
        Evaluacion ev = evaluaciones.findByPostulacionIdAndEvaluadorId(postulacionId, evaluadorId)
                .orElseGet(() -> new Evaluacion(p, evaluador));
        ev.calificar(d.tecnico(), d.perfil(), d.academico(), d.portafolio(), d.blandas(), d.entrevista(),
                Textos.limpiar(d.concepto()), d.recomendado());
        evaluaciones.save(ev);
        actividad.registrar(evaluadorId, "EVALUAR_CANDIDATO", "postulacion", postulacionId,
                ev.getPuntajeFinal() + " puntos" + (d.recomendado() ? " · recomendado" : ""));
        return candidato(p, evaluadorId);
    }

    /** RF-24: el comité también puede mover la postulación en el flujo. */
    public CandidatoDto cambiarEstado(Long evaluadorId, Long postulacionId, CambioEstadoRequest datos) {
        Postulacion p = postulacionService.buscar(postulacionId);
        permitido(evaluadorId, p.getEvaluador());
        postulacionService.cambiarEstado(p, datos, evaluadorId);
        return candidato(p, evaluadorId);
    }

    // ---------- Historial y actas (RF-33) ----------

    @Transactional(readOnly = true)
    public HistorialDto historial(Long evaluadorId) {
        return new HistorialDto(
                evaluacionesOferta.findByEvaluadorIdOrderByFechaDesc(evaluadorId).stream().map(EvaluacionOfertaDto::de).toList(),
                evaluaciones.findByEvaluadorIdOrderByFechaDesc(evaluadorId).stream().map(EvaluacionCandidatoDto::de).toList());
    }

    @Transactional(readOnly = true)
    public ActaDto acta(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial.");
        }
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1);
        List<EvaluacionOfertaDto> ofertasActa = evaluacionesOferta.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .map(EvaluacionOfertaDto::de).toList();
        List<EvaluacionCandidatoDto> candidatosActa = evaluaciones.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .map(EvaluacionCandidatoDto::de).toList();
        Set<String> evaluadores = new TreeSet<>();
        Stream.concat(ofertasActa.stream().map(EvaluacionOfertaDto::evaluador),
                candidatosActa.stream().map(EvaluacionCandidatoDto::evaluador)).forEach(evaluadores::add);
        return new ActaDto(desde, hasta, LocalDateTime.now(), puntajeMinimo, List.copyOf(evaluadores),
                ofertasActa, candidatosActa);
    }

    public BigDecimal getPuntajeMinimo() {
        return puntajeMinimo;
    }

    // ---------- Utilidades ----------

    /** RF-34: si el administrador asignó un evaluador, solo esa persona califica. */
    private Usuario permitido(Long evaluadorId, Usuario asignado) {
        if (asignado != null && !asignado.getId().equals(evaluadorId)) {
            throw new NegocioException(HttpStatus.FORBIDDEN, "Esta evaluación está asignada a otro miembro del comité.");
        }
        return usuarios.findById(evaluadorId)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Tu sesión expiró. Inicia sesión de nuevo."));
    }

    private OfertaParaEvaluarDto paraEvaluar(OfertaPractica o) {
        return new OfertaParaEvaluarDto(ofertaService.dto(o), PerfilEmpresaDto.de(o.getEmpresa()),
                evaluacionesOferta.findByOfertaIdOrderByFechaDesc(o.getId()).stream().map(EvaluacionOfertaDto::de).toList(),
                puntajeMinimo);
    }

    private CandidatoDto candidato(Postulacion p, Long evaluadorId) {
        PostulanteDto postulante = postulacionService.postulanteDto(p);
        EvaluacionCandidatoDto mia = evaluaciones.findByPostulacionIdAndEvaluadorId(p.getId(), evaluadorId)
                .map(EvaluacionCandidatoDto::de).orElse(null);
        return new CandidatoDto(postulante, p.getOferta().getEmpresa().getNombreVisible(), mia);
    }
}
