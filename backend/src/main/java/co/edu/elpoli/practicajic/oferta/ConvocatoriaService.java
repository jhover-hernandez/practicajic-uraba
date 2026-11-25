package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.estudiante.EstudianteCompetencia;
import co.edu.elpoli.practicajic.estudiante.PerfilEstudianteService;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.*;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** RF-17, RF-18 y RF-25: el estudiante busca convocatorias y ve qué competencias suyas coinciden. */
@Service
@Transactional(readOnly = true)
public class ConvocatoriaService {

    private final OfertaRepository ofertas;
    private final OfertaService ofertaService;
    private final PerfilEstudianteService perfilEstudiante;
    private final PostulacionRepository postulaciones;

    public ConvocatoriaService(OfertaRepository ofertas, OfertaService ofertaService,
                               PerfilEstudianteService perfilEstudiante, PostulacionRepository postulaciones) {
        this.ofertas = ofertas;
        this.ofertaService = ofertaService;
        this.perfilEstudiante = perfilEstudiante;
        this.postulaciones = postulaciones;
    }

    public List<ConvocatoriaDto> buscar(Long usuarioId, String texto, Long areaId, String municipio, Modalidad modalidad) {
        Estudiante estudiante = perfilEstudiante.delUsuario(usuarioId);
        Set<Long> mias = idsCompetencias(perfilEstudiante.competenciasDe(estudiante.getId()));

        // RF-19 y RN-02: solo se ven ofertas aprobadas por el comité y con la fecha de cierre vigente.
        Specification<OfertaPractica> filtro = (o, q, cb) -> cb.and(
                cb.equal(o.get("estado"), EstadoOferta.APROBADA),
                cb.or(cb.isNull(o.get("fechaCierre")),
                        cb.greaterThanOrEqualTo(o.<LocalDate>get("fechaCierre"), LocalDate.now())));
        if (areaId != null) {
            filtro = filtro.and((o, q, cb) -> cb.equal(o.get("area").get("id"), areaId));
        }
        if (municipio != null && !municipio.isBlank()) {
            filtro = filtro.and((o, q, cb) -> cb.equal(o.get("municipio"), municipio.trim()));
        }
        if (modalidad != null) {
            filtro = filtro.and((o, q, cb) -> cb.equal(o.get("modalidad"), modalidad));
        }
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
            filtro = filtro.and((o, q, cb) -> cb.or(
                    cb.like(cb.lower(o.<String>get("titulo")), patron),
                    cb.like(cb.lower(o.<String>get("descripcion")), patron),
                    cb.like(cb.lower(o.get("empresa").<String>get("razonSocial")), patron),
                    cb.like(cb.lower(cb.coalesce(o.get("empresa").<String>get("nombreComercial"), "")), patron)));
        }
        return ofertas.findAll(filtro, Sort.by(Sort.Direction.DESC, "fechaPublicacion", "id")).stream()
                .map(o -> dto(o, estudiante, mias))
                .toList();
    }

    /** RF-18. También se puede consultar una oferta ya cerrada si el estudiante se postuló a ella. */
    public ConvocatoriaDto detalle(Long usuarioId, Long ofertaId) {
        Estudiante estudiante = perfilEstudiante.delUsuario(usuarioId);
        OfertaPractica oferta = ofertas.findById(ofertaId)
                .filter(o -> o.getEstado() == EstadoOferta.APROBADA || o.getEstado() == EstadoOferta.CERRADA)
                .filter(o -> o.estaAbierta() || postulaciones.existsByOfertaIdAndEstudianteId(o.getId(), estudiante.getId()))
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La convocatoria no existe o ya cerró."));
        return dto(oferta, estudiante, idsCompetencias(perfilEstudiante.competenciasDe(estudiante.getId())));
    }

    /** RF-25: competencias de la oferta que el estudiante tiene registradas (en cualquier nivel). */
    public static List<Long> coincidentes(OfertaPractica oferta, Set<Long> competenciasEstudiante) {
        return oferta.getCompetencias().stream()
                .map(oc -> oc.getCompetencia().getId())
                .filter(competenciasEstudiante::contains)
                .toList();
    }

    public static Set<Long> idsCompetencias(Collection<EstudianteCompetencia> lista) {
        return lista.stream().map(ec -> ec.getCompetencia().getId()).collect(Collectors.toSet());
    }

    private ConvocatoriaDto dto(OfertaPractica o, Estudiante estudiante, Set<Long> mias) {
        OfertaDto oferta = ofertaService.dto(o);
        Empresa e = o.getEmpresa();
        List<Long> comunes = coincidentes(o, mias);
        return new ConvocatoriaDto(oferta,
                new EmpresaPublicaDto(e.getNombreVisible(), e.getRazonSocial(), e.getSector(), e.getTamano(), e.getMunicipio()),
                comunes.size(), comunes,
                postulaciones.existsByOfertaIdAndEstudianteId(o.getId(), estudiante.getId()),
                oferta.vacantesCubiertas() < o.getVacantes());
    }
}
