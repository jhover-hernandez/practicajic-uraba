package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.comite.Evaluacion;
import co.edu.elpoli.practicajic.comite.EvaluacionRepository;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.PerfilEmpresaService;
import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.PerfilEstudianteDto;
import co.edu.elpoli.practicajic.estudiante.PerfilEstudianteService;
import co.edu.elpoli.practicajic.oferta.ConvocatoriaService;
import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.oferta.OfertaService;
import co.edu.elpoli.practicajic.postulacion.PostulacionDtos.*;
import co.edu.elpoli.practicajic.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** RF-10 y RF-22 a RF-27: postulaciones, flujo de estados, entrevistas y aceptación de la práctica. */
@Service
@Transactional
public class PostulacionService {

    private static final Set<EstadoPostulacion> PRACTICA_ACEPTADA_O_ACTIVA =
            EnumSet.of(EstadoPostulacion.SELECCIONADO, EstadoPostulacion.PRACTICA_INICIADA);

    private final PostulacionRepository postulaciones;
    private final EvaluacionRepository evaluaciones;
    private final OfertaService ofertas;
    private final PerfilEstudianteService perfilEstudiante;
    private final PerfilEmpresaService perfilEmpresa;
    private final ActividadService actividad;
    private final CorreoService correo;

    public PostulacionService(PostulacionRepository postulaciones, EvaluacionRepository evaluaciones,
                              OfertaService ofertas, PerfilEstudianteService perfilEstudiante,
                              PerfilEmpresaService perfilEmpresa, ActividadService actividad, CorreoService correo) {
        this.postulaciones = postulaciones;
        this.evaluaciones = evaluaciones;
        this.ofertas = ofertas;
        this.perfilEstudiante = perfilEstudiante;
        this.perfilEmpresa = perfilEmpresa;
        this.actividad = actividad;
        this.correo = correo;
    }

    // ---------- Estudiante ----------

    public MiPostulacionDto postular(Long usuarioId, Long ofertaId) {
        Estudiante estudiante = perfilEstudiante.delUsuario(usuarioId);
        OfertaPractica oferta = ofertas.buscar(ofertaId);
        if (!perfilEstudiante.puedePostularse(estudiante)) {
            throw new NegocioException(HttpStatus.FORBIDDEN,
                    "Para postularte necesitas estar validado por el Politécnico y tener tu hoja de vida cargada.");
        }
        if (!oferta.estaAbierta()) {
            throw new NegocioException(HttpStatus.CONFLICT, "Esta convocatoria ya no recibe postulaciones.");
        }
        if (postulaciones.existsByOfertaIdAndEstudianteId(ofertaId, estudiante.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya te postulaste a esta convocatoria.");
        }
        if (vacantesCubiertas(oferta)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Las vacantes de esta convocatoria ya están cubiertas.");
        }
        if (tienePracticaAceptada(estudiante.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya aceptaste una práctica; no puedes postularte a otras.");
        }
        int comunes = ConvocatoriaService.coincidentes(oferta,
                ConvocatoriaService.idsCompetencias(perfilEstudiante.competenciasDe(estudiante.getId()))).size();
        Postulacion p = postulaciones.save(new Postulacion(oferta, estudiante, (short) comunes));
        actividad.registrar(usuarioId, "POSTULAR", "postulacion", p.getId(), oferta.getTitulo());
        correo.enviarAviso(oferta.getEmpresa().getUsuario().getCorreo(), "Nuevo postulante",
                estudiante.getNombreCompleto() + " se postuló a la convocatoria \"" + oferta.getTitulo() + "\".");
        return miDto(p);
    }

    @Transactional(readOnly = true)
    public List<MiPostulacionDto> mias(Long usuarioId) {
        Estudiante estudiante = perfilEstudiante.delUsuario(usuarioId);
        return postulaciones.findByEstudianteIdOrderByFechaDesc(estudiante.getId()).stream().map(this::miDto).toList();
    }

    /** RF-27 y RN-08: al aceptar una práctica, las demás postulaciones del estudiante pasan a "No seleccionado". */
    public MiPostulacionDto aceptar(Long usuarioId, Long id) {
        Estudiante estudiante = perfilEstudiante.delUsuario(usuarioId);
        Postulacion p = postulaciones.findById(id)
                .filter(x -> x.getEstudiante().getId().equals(estudiante.getId()))
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La postulación no existe."));
        if (p.getEstado() != EstadoPostulacion.SELECCIONADO || p.getFechaAceptacion() != null) {
            throw new NegocioException(HttpStatus.CONFLICT, "Solo puedes aceptar una práctica en la que fuiste seleccionado.");
        }
        if (tienePracticaAceptada(estudiante.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya aceptaste otra práctica. Solo puedes tener una activa.");
        }
        p.setFechaAceptacion(LocalDateTime.now());
        actividad.registrar(usuarioId, "ACEPTAR_PRACTICA", "postulacion", id, p.getOferta().getTitulo());

        for (Postulacion otra : postulaciones.findByEstudianteIdAndEstadoIn(estudiante.getId(),
                EnumSet.of(EstadoPostulacion.POSTULADO, EstadoPostulacion.EN_REVISION, EstadoPostulacion.PRESELECCIONADO,
                        EstadoPostulacion.ENTREVISTA, EstadoPostulacion.SELECCIONADO))) {
            if (!otra.getId().equals(id)) {
                otra.setEstado(EstadoPostulacion.NO_SELECCIONADO);
                otra.setObservaciones("El estudiante aceptó otra práctica.");
                actividad.registrar(null, "CAMBIAR_ESTADO_POSTULACION", "postulacion", otra.getId(),
                        "NO_SELECCIONADO · aceptó otra práctica (RN-08)");
            }
        }
        correo.enviarAviso(p.getOferta().getEmpresa().getUsuario().getCorreo(), "Práctica aceptada",
                estudiante.getNombreCompleto() + " aceptó la práctica \"" + p.getOferta().getTitulo()
                        + "\". El Politécnico formalizará la vinculación.");
        return miDto(p);
    }

    // ---------- Empresa ----------

    @Transactional(readOnly = true)
    public List<PostulanteDto> postulantesDeOferta(Long usuarioId, Long ofertaId) {
        Empresa empresa = perfilEmpresa.delUsuario(usuarioId);
        OfertaPractica oferta = ofertas.buscar(ofertaId);
        if (!oferta.getEmpresa().getId().equals(empresa.getId())) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "La oferta no existe.");
        }
        return postulaciones.findByOfertaIdOrderByCompetenciasCoincidentesDescFechaAsc(ofertaId).stream()
                .map(this::postulanteDto)
                .sorted(Comparator.comparingInt(PostulanteDto::coincidentes).reversed())
                .toList();
    }

    public PostulanteDto cambiarEstadoPorEmpresa(Long usuarioId, Long id, CambioEstadoRequest datos) {
        Postulacion p = deMiEmpresa(usuarioId, id);
        cambiarEstado(p, datos, usuarioId);
        return postulanteDto(p);
    }

    /** RF-26: reprograma la entrevista de un candidato que ya está en estado Entrevista. */
    public PostulanteDto programarEntrevista(Long usuarioId, Long id, EntrevistaRequest datos) {
        Postulacion p = deMiEmpresa(usuarioId, id);
        if (p.getEstado() == EstadoPostulacion.PRESELECCIONADO) {
            cambiarEstado(p, new CambioEstadoRequest(EstadoPostulacion.ENTREVISTA, null, datos.fechaEntrevista(),
                    datos.lugarEntrevista()), usuarioId);
        } else if (p.getEstado() == EstadoPostulacion.ENTREVISTA) {
            p.setFechaEntrevista(datos.fechaEntrevista());
            p.setLugarEntrevista(Textos.limpiar(datos.lugarEntrevista()));
            actividad.registrar(usuarioId, "PROGRAMAR_ENTREVISTA", "postulacion", id, datos.fechaEntrevista().toString());
            avisarEstudiante(p, "Tu entrevista para \"" + p.getOferta().getTitulo() + "\" se reprogramó para el "
                    + Mensajes.legible(datos.fechaEntrevista()) + lugar(p) + ".");
        } else {
            throw new NegocioException(HttpStatus.CONFLICT, "Solo se programan entrevistas a candidatos preseleccionados.");
        }
        return postulanteDto(p);
    }

    /** RNF-07: la empresa solo ve la hoja de vida de quien se postuló a sus ofertas. */
    @Transactional(readOnly = true)
    public PerfilEstudianteService.HojaVidaArchivo hojaVidaParaEmpresa(Long usuarioId, Long id) {
        return perfilEstudiante.hojaVidaDe(deMiEmpresa(usuarioId, id).getEstudiante().getId());
    }

    // ---------- Compartido con comité y administración ----------

    public Postulacion buscar(Long id) {
        return postulaciones.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La postulación no existe."));
    }

    /** RF-24: aplica el flujo definido. Ningún estado retrocede. */
    public void cambiarEstado(Postulacion p, CambioEstadoRequest datos, Long actorId) {
        EstadoPostulacion actual = p.getEstado();
        EstadoPostulacion nuevo = datos.estado();
        if (!actual.puedePasarA(nuevo)) {
            throw new NegocioException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + Mensajes.estado(actual) + " a " + Mensajes.estado(nuevo) + ".");
        }
        if (nuevo == EstadoPostulacion.ENTREVISTA) {
            if (datos.fechaEntrevista() == null) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Indica la fecha y hora de la entrevista.");
            }
            p.setFechaEntrevista(datos.fechaEntrevista());
            p.setLugarEntrevista(Textos.limpiar(datos.lugarEntrevista()));
        }
        if (nuevo == EstadoPostulacion.SELECCIONADO && vacantesCubiertas(p.getOferta())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Las vacantes de esta convocatoria ya están cubiertas.");
        }
        p.setEstado(nuevo);
        p.setObservaciones(Textos.limpiar(datos.observaciones()));
        actividad.registrar(actorId, "CAMBIAR_ESTADO_POSTULACION", "postulacion", p.getId(), actual + " → " + nuevo);

        String oferta = "\"" + p.getOferta().getTitulo() + "\"";
        avisarEstudiante(p, switch (nuevo) {
            case ENTREVISTA -> "Tienes entrevista para " + oferta + " el " + Mensajes.legible(p.getFechaEntrevista()) + lugar(p) + ".";
            case SELECCIONADO -> "¡Fuiste seleccionado para " + oferta + "! Ingresa a Mis postulaciones para aceptar la práctica.";
            case NO_SELECCIONADO -> "Tu postulación a " + oferta + " no continúa en el proceso."
                    + (p.getObservaciones() != null ? " Observaciones: " + p.getObservaciones() : "");
            default -> "Tu postulación a " + oferta + " pasó a " + Mensajes.estado(nuevo) + ".";
        });
    }

    public PostulanteDto asignarEvaluador(Long id, Usuario evaluador, Long actorId) {
        Postulacion p = buscar(id);
        p.setEvaluador(evaluador);
        actividad.registrar(actorId, "ASIGNAR_EVALUADOR", "postulacion", id, evaluador.getCorreo());
        correo.enviarAviso(evaluador.getCorreo(), "Nuevo candidato por calificar",
                "Se te asignó calificar a " + p.getEstudiante().getNombreCompleto() + " en la convocatoria \""
                        + p.getOferta().getTitulo() + "\".");
        return postulanteDto(p);
    }

    public PostulanteDto postulanteDto(Postulacion p) {
        Estudiante e = p.getEstudiante();
        PerfilEstudianteDto perfil = perfilEstudiante.dto(e);
        List<Long> comunes = ConvocatoriaService.coincidentes(p.getOferta(),
                ConvocatoriaService.idsCompetencias(perfilEstudiante.competenciasDe(e.getId())));
        Usuario ev = p.getEvaluador();
        return new PostulanteDto(p.getId(), p.getOferta().getId(), p.getOferta().getTitulo(), e.getId(),
                e.getNombreCompleto(), e.getUsuario().getCorreo(), e.getPrograma(), e.getSemestre(), e.getMunicipio(),
                e.getDisponibilidad(), p.getFecha(), p.getEstado(), p.getEstado().siguiente(), comunes.size(),
                p.getOferta().getCompetencias().size(), comunes, perfil.competencias(), perfil.portafolio(),
                perfil.hojaVida() != null, p.getFechaEntrevista(), p.getLugarEntrevista(), p.getObservaciones(),
                p.getFechaAceptacion(), ev == null ? null : ev.getId(), ev == null ? null : ev.getNombre(),
                resumenComite(p.getId()));
    }

    @Transactional(readOnly = true)
    public List<PostulanteDto> todas(EstadoPostulacion estado) {
        List<Postulacion> lista = estado == null
                ? postulaciones.findByEstadoInOrderByFechaAsc(EnumSet.allOf(EstadoPostulacion.class))
                : postulaciones.findByEstadoInOrderByFechaAsc(EnumSet.of(estado));
        return lista.stream().map(this::postulanteDto).toList();
    }

    // ---------- Utilidades ----------

    private Postulacion deMiEmpresa(Long usuarioId, Long id) {
        Empresa empresa = perfilEmpresa.delUsuario(usuarioId);
        Postulacion p = buscar(id);
        if (!p.getOferta().getEmpresa().getId().equals(empresa.getId())) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "La postulación no existe.");
        }
        return p;
    }

    private boolean vacantesCubiertas(OfertaPractica oferta) {
        return postulaciones.countByOfertaIdAndEstadoIn(oferta.getId(), EstadoPostulacion.OCUPAN_VACANTE)
                >= oferta.getVacantes();
    }

    private boolean tienePracticaAceptada(Long estudianteId) {
        return postulaciones.findByEstudianteIdAndEstadoIn(estudianteId, PRACTICA_ACEPTADA_O_ACTIVA).stream()
                .anyMatch(x -> x.getFechaAceptacion() != null || x.getEstado() == EstadoPostulacion.PRACTICA_INICIADA);
    }

    private ResumenComiteDto resumenComite(Long postulacionId) {
        List<Evaluacion> lista = evaluaciones.findByPostulacionIdOrderByFechaAsc(postulacionId);
        if (lista.isEmpty()) {
            return null;
        }
        BigDecimal suma = lista.stream().map(Evaluacion::getPuntajeFinal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long recomiendan = lista.stream().filter(Evaluacion::isRecomendado).count();
        List<String> conceptos = lista.stream().filter(ev -> ev.getConcepto() != null)
                .map(ev -> ev.getEvaluador().getNombre() + ": " + ev.getConcepto()).toList();
        return new ResumenComiteDto(lista.size(), suma.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP),
                recomiendan * 2 > lista.size(), conceptos);
    }

    private MiPostulacionDto miDto(Postulacion p) {
        OfertaPractica o = p.getOferta();
        int comunes = ConvocatoriaService.coincidentes(o,
                ConvocatoriaService.idsCompetencias(perfilEstudiante.competenciasDe(p.getEstudiante().getId()))).size();
        return new MiPostulacionDto(p.getId(), o.getId(), o.getTitulo(), o.getEmpresa().getNombreVisible(),
                o.getMunicipio(), o.getModalidad(), p.getFecha(), p.getEstado(), comunes, o.getCompetencias().size(),
                p.getFechaEntrevista(), p.getLugarEntrevista(), p.getObservaciones(), p.getFechaAceptacion(),
                p.getEstado() == EstadoPostulacion.SELECCIONADO && p.getFechaAceptacion() == null);
    }

    private void avisarEstudiante(Postulacion p, String mensaje) {
        correo.enviarAviso(p.getEstudiante().getUsuario().getCorreo(), "Novedad en tu postulación", mensaje);
    }

    private static String lugar(Postulacion p) {
        return p.getLugarEntrevista() != null ? " en " + p.getLugarEntrevista() : "";
    }
}
