package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.catalogo.CatalogoService;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.EstadoValidacionEmpresa;
import co.edu.elpoli.practicajic.empresa.PerfilEmpresaService;
import co.edu.elpoli.practicajic.oferta.OfertaDtos.*;
import co.edu.elpoli.practicajic.postulacion.EstadoPostulacion;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import co.edu.elpoli.practicajic.usuario.Rol;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** RF-08, RF-09 y RF-34: la empresa publica necesidades; el administrador asigna evaluador y puede cerrarlas. */
@Service
@Transactional
public class OfertaService {

    private static final Set<EstadoOferta> EDITABLES = EnumSet.of(EstadoOferta.BORRADOR, EstadoOferta.AJUSTES_SOLICITADOS);

    private final OfertaRepository ofertas;
    private final PostulacionRepository postulaciones;
    private final PerfilEmpresaService perfilEmpresa;
    private final CatalogoService catalogo;
    private final UsuarioRepository usuarios;
    private final ActividadService actividad;
    private final CorreoService correo;

    public OfertaService(OfertaRepository ofertas, PostulacionRepository postulaciones,
                         PerfilEmpresaService perfilEmpresa, CatalogoService catalogo, UsuarioRepository usuarios,
                         ActividadService actividad, CorreoService correo) {
        this.ofertas = ofertas;
        this.postulaciones = postulaciones;
        this.perfilEmpresa = perfilEmpresa;
        this.catalogo = catalogo;
        this.usuarios = usuarios;
        this.actividad = actividad;
        this.correo = correo;
    }

    // ---------- Empresa ----------

    @Transactional(readOnly = true)
    public List<OfertaDto> deEmpresa(Long usuarioId) {
        Empresa empresa = perfilEmpresa.delUsuario(usuarioId);
        return ofertas.findByEmpresaIdOrderByFechaCreacionDesc(empresa.getId()).stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public OfertaDto propia(Long usuarioId, Long id) {
        return dto(buscarPropia(usuarioId, id));
    }

    public OfertaDto crear(Long usuarioId, OfertaRequest datos) {
        Empresa empresa = perfilEmpresa.delUsuario(usuarioId);
        // RN-01: solo una empresa validada publica ofertas.
        if (empresa.getEstadoValidacion() != EstadoValidacionEmpresa.VALIDADA) {
            throw new NegocioException(HttpStatus.FORBIDDEN,
                    "Tu empresa debe estar validada por el Politécnico antes de publicar ofertas.");
        }
        OfertaPractica oferta = new OfertaPractica(empresa);
        aplicar(oferta, datos);
        oferta = ofertas.save(oferta);
        actividad.registrar(usuarioId, "CREAR_OFERTA", "oferta_practica", oferta.getId(), oferta.getTitulo());
        if (datos.enviarARevision()) {
            enviarARevision(oferta, usuarioId);
        }
        return dto(oferta);
    }

    public OfertaDto editar(Long usuarioId, Long id, OfertaRequest datos) {
        OfertaPractica oferta = buscarPropia(usuarioId, id);
        if (!EDITABLES.contains(oferta.getEstado())) {
            throw new NegocioException(HttpStatus.CONFLICT,
                    "Solo puedes editar ofertas en borrador o con ajustes solicitados por el comité.");
        }
        aplicar(oferta, datos);
        actividad.registrar(usuarioId, "EDITAR_OFERTA", "oferta_practica", id, null);
        if (datos.enviarARevision()) {
            enviarARevision(oferta, usuarioId);
        }
        return dto(oferta);
    }

    public OfertaDto enviar(Long usuarioId, Long id) {
        OfertaPractica oferta = buscarPropia(usuarioId, id);
        if (!EDITABLES.contains(oferta.getEstado())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Esta oferta ya fue enviada al comité.");
        }
        enviarARevision(oferta, usuarioId);
        return dto(oferta);
    }

    public OfertaDto cerrarPropia(Long usuarioId, Long id) {
        OfertaPractica oferta = buscarPropia(usuarioId, id);
        cerrar(oferta, usuarioId, "Cerrada por la empresa");
        return dto(oferta);
    }

    public void eliminar(Long usuarioId, Long id) {
        OfertaPractica oferta = buscarPropia(usuarioId, id);
        if (oferta.getEstado() != EstadoOferta.BORRADOR) {
            throw new NegocioException(HttpStatus.CONFLICT, "Solo se pueden eliminar ofertas en borrador.");
        }
        ofertas.delete(oferta);
        actividad.registrar(usuarioId, "ELIMINAR_OFERTA", "oferta_practica", id, oferta.getTitulo());
    }

    // ---------- Administrador ----------

    @Transactional(readOnly = true)
    public List<OfertaDto> todas(EstadoOferta estado) {
        List<OfertaPractica> lista = estado == null
                ? ofertas.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"))
                : ofertas.findByEstadoOrderByFechaCreacionAsc(estado);
        return lista.stream().map(this::dto).toList();
    }

    public OfertaDto asignarEvaluador(Long id, AsignarEvaluadorRequest datos, Long actorId) {
        OfertaPractica oferta = buscar(id);
        Usuario evaluador = evaluadorValido(datos.evaluadorId());
        oferta.setEvaluador(evaluador);
        actividad.registrar(actorId, "ASIGNAR_EVALUADOR", "oferta_practica", id, evaluador.getCorreo());
        correo.enviarAviso(evaluador.getCorreo(), "Nueva oferta por evaluar",
                "Se te asignó la evaluación de la oferta \"" + oferta.getTitulo() + "\" de "
                        + oferta.getEmpresa().getNombreVisible() + ".");
        return dto(oferta);
    }

    public OfertaDto cerrarAdmin(Long id, Long actorId) {
        OfertaPractica oferta = buscar(id);
        cerrar(oferta, actorId, "Cerrada por el administrador");
        return dto(oferta);
    }

    /** RF-20: cierra las convocatorias cuya fecha de cierre ya pasó. Devuelve cuántas cerró. */
    public int cerrarVencidas() {
        List<OfertaPractica> vencidas = ofertas.findByEstadoAndFechaCierreBefore(EstadoOferta.APROBADA, LocalDate.now());
        for (OfertaPractica oferta : vencidas) {
            oferta.setEstado(EstadoOferta.CERRADA);
            actividad.registrar(null, "CERRAR_OFERTA", "oferta_practica", oferta.getId(), "Cierre automático por fecha");
        }
        return vencidas.size();
    }

    // ---------- Compartido ----------

    public OfertaPractica buscar(Long id) {
        return ofertas.findById(id).orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La oferta no existe."));
    }

    public Usuario evaluadorValido(Long usuarioId) {
        Usuario u = usuarios.findById(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El evaluador no existe."));
        if (u.getRol() != Rol.COMITE || !u.isActivo()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "El evaluador debe ser un miembro activo del comité.");
        }
        return u;
    }

    public OfertaDto dto(OfertaPractica o) {
        Usuario ev = o.getEvaluador();
        return new OfertaDto(o.getId(), o.getTitulo(), o.getEmpresa().getId(), o.getEmpresa().getNombreVisible(),
                o.getArea().getId(), o.getArea().getNombre(), o.getDescripcion(), o.getPerfil(), o.getActividades(),
                o.getModalidad(), o.getMunicipio(), o.getDuracionMeses(), o.getVacantes(), o.getHorario(),
                o.getTipoVinculacion(), o.getIncentivo(), o.getFechaPublicacion(), o.getFechaCierre(), o.getEstado(),
                o.estaAbierta(), o.getObservacionesComite(), o.getFechaCreacion(),
                o.getCompetencias().stream().map(CompetenciaOfertaDto::de).toList(),
                postulaciones.countByOfertaId(o.getId()),
                postulaciones.countByOfertaIdAndEstadoIn(o.getId(), EstadoPostulacion.OCUPAN_VACANTE),
                ev == null ? null : ev.getId(), ev == null ? null : ev.getNombre());
    }

    private OfertaPractica buscarPropia(Long usuarioId, Long id) {
        Empresa empresa = perfilEmpresa.delUsuario(usuarioId);
        return ofertas.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La oferta no existe."));
    }

    private void enviarARevision(OfertaPractica oferta, Long usuarioId) {
        if (oferta.getEmpresa().getEstadoValidacion() != EstadoValidacionEmpresa.VALIDADA) {
            throw new NegocioException(HttpStatus.FORBIDDEN,
                    "Tu empresa debe estar validada por el Politécnico antes de publicar ofertas.");
        }
        if (oferta.getFechaCierre() == null || oferta.getFechaCierre().isBefore(LocalDate.now())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "La fecha de cierre no puede estar en el pasado.");
        }
        oferta.setEstado(EstadoOferta.EN_REVISION);
        actividad.registrar(usuarioId, "ENVIAR_OFERTA_REVISION", "oferta_practica", oferta.getId(), null);
    }

    private void cerrar(OfertaPractica oferta, Long actorId, String detalle) {
        if (oferta.getEstado() != EstadoOferta.APROBADA) {
            throw new NegocioException(HttpStatus.CONFLICT, "Solo se pueden cerrar convocatorias abiertas.");
        }
        oferta.setEstado(EstadoOferta.CERRADA);
        actividad.registrar(actorId, "CERRAR_OFERTA", "oferta_practica", oferta.getId(), detalle);
    }

    private void aplicar(OfertaPractica o, OfertaRequest d) {
        o.setArea(catalogo.buscarArea(d.areaId()));
        o.setTitulo(d.titulo().trim());
        o.setDescripcion(d.descripcion().trim());
        o.setPerfil(Textos.limpiar(d.perfil()));
        o.setActividades(d.actividades().trim());
        o.setModalidad(d.modalidad());
        o.setMunicipio(d.municipio().trim());
        o.setDuracionMeses(d.duracionMeses());
        o.setVacantes(d.vacantes());
        o.setHorario(Textos.limpiar(d.horario()));
        o.setTipoVinculacion(Textos.limpiar(d.tipoVinculacion()));
        o.setIncentivo(d.incentivo());
        o.setFechaCierre(d.fechaCierre());

        Set<Long> vistas = new HashSet<>();
        for (CompetenciaRequerida c : d.competencias()) {
            if (!vistas.add(c.competenciaId())) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "No repitas la misma competencia.");
            }
        }
        // Se borran primero las anteriores: Hibernate inserta antes de borrar y chocaría con la restricción única.
        if (!o.getCompetencias().isEmpty()) {
            o.getCompetencias().clear();
            ofertas.saveAndFlush(o);
        }
        for (CompetenciaRequerida c : d.competencias()) {
            o.getCompetencias().add(new OfertaCompetencia(o, catalogo.buscarCompetencia(c.competenciaId()), c.nivelRequerido()));
        }
    }
}
