package co.edu.elpoli.practicajic.estudiante;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.archivo.AlmacenArchivos;
import co.edu.elpoli.practicajic.catalogo.CatalogoService;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** RF-12 a RF-15: perfil digital, hoja de vida, competencias y portafolio del estudiante. */
@Service
@Transactional
public class PerfilEstudianteService {

    private final EstudianteRepository estudiantes;
    private final HojaVidaRepository hojasVida;
    private final EstudianteCompetenciaRepository competenciasEstudiante;
    private final PortafolioRepository portafolios;
    private final CatalogoService catalogo;
    private final AlmacenArchivos almacen;
    private final ActividadService actividad;

    public PerfilEstudianteService(EstudianteRepository estudiantes, HojaVidaRepository hojasVida,
                                   EstudianteCompetenciaRepository competenciasEstudiante,
                                   PortafolioRepository portafolios, CatalogoService catalogo,
                                   AlmacenArchivos almacen, ActividadService actividad) {
        this.estudiantes = estudiantes;
        this.hojasVida = hojasVida;
        this.competenciasEstudiante = competenciasEstudiante;
        this.portafolios = portafolios;
        this.catalogo = catalogo;
        this.almacen = almacen;
        this.actividad = actividad;
    }

    @Transactional(readOnly = true)
    public PerfilEstudianteDto ver(Long usuarioId) {
        return dto(delUsuario(usuarioId));
    }

    public PerfilEstudianteDto actualizar(Long usuarioId, ActualizarPerfilRequest datos) {
        Estudiante e = delUsuario(usuarioId);
        e.setNombres(datos.nombres().trim());
        e.setApellidos(datos.apellidos().trim());
        e.setPrograma(datos.programa().trim());
        e.setSemestre(datos.semestre());
        e.setMunicipio(Textos.limpiar(datos.municipio()));
        e.setDisponibilidad(Textos.limpiar(datos.disponibilidad()));
        e.getUsuario().setNombre(e.getNombreCompleto());
        volverAPendienteSiFueRechazado(e, usuarioId);
        return dto(e);
    }

    public PerfilEstudianteDto subirHojaVida(Long usuarioId, MultipartFile archivo) {
        Estudiante e = delUsuario(usuarioId);
        String nombre = almacen.guardarPdf(archivo);
        int version = hojasVida.findFirstByEstudianteIdOrderByVersionDesc(e.getId())
                .map(h -> h.getVersion() + 1).orElse(1);
        hojasVida.desactivarTodas(e.getId());
        HojaVida hoja = hojasVida.save(new HojaVida(e, nombre, version, nombreOriginal(archivo), archivo.getSize()));
        actividad.registrar(usuarioId, "CARGAR_HOJA_VIDA", "hoja_vida", hoja.getId(), "Versión " + version);
        return dto(e);
    }

    @Transactional(readOnly = true)
    public HojaVidaArchivo hojaVidaPropia(Long usuarioId) {
        return hojaVidaDe(delUsuario(usuarioId).getId());
    }

    /** Archivo activo del estudiante. Quien llama debe comprobar antes que tiene permiso de verlo (RNF-07). */
    @Transactional(readOnly = true)
    public HojaVidaArchivo hojaVidaDe(Long estudianteId) {
        HojaVida hoja = hojasVida.findFirstByEstudianteIdAndActivaTrue(estudianteId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El estudiante no ha cargado hoja de vida."));
        return new HojaVidaArchivo(almacen.cargar(hoja.getArchivo()), hoja.getNombreOriginal());
    }

    public PerfilEstudianteDto guardarCompetencias(Long usuarioId, CompetenciasRequest datos) {
        Estudiante e = delUsuario(usuarioId);
        Set<Long> vistas = new HashSet<>();
        for (CompetenciaItem item : datos.competencias()) {
            if (!vistas.add(item.competenciaId())) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "No repitas la misma competencia.");
            }
        }
        competenciasEstudiante.eliminarDe(e.getId());
        for (CompetenciaItem item : datos.competencias()) {
            competenciasEstudiante.save(
                    new EstudianteCompetencia(e, catalogo.buscarCompetencia(item.competenciaId()), item.nivel()));
        }
        return dto(e);
    }

    public PortafolioDto agregarPortafolio(Long usuarioId, PortafolioRequest datos) {
        Portafolio p = new Portafolio(delUsuario(usuarioId));
        aplicar(p, datos);
        return PortafolioDto.de(portafolios.save(p));
    }

    public PortafolioDto editarPortafolio(Long usuarioId, Long id, PortafolioRequest datos) {
        Portafolio p = portafolioPropio(usuarioId, id);
        aplicar(p, datos);
        return PortafolioDto.de(p);
    }

    public void eliminarPortafolio(Long usuarioId, Long id) {
        portafolios.delete(portafolioPropio(usuarioId, id));
    }

    // ---------- Consultas usadas por otros módulos ----------

    public Estudiante delUsuario(Long usuarioId) {
        return estudiantes.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "No se encontró el perfil de estudiante."));
    }

    /** RN-05: validado por el Politécnico y con hoja de vida activa. */
    public boolean puedePostularse(Estudiante e) {
        return e.getEstado() == EstadoEstudiante.VALIDADO && hojasVida.existsByEstudianteIdAndActivaTrue(e.getId());
    }

    public List<EstudianteCompetencia> competenciasDe(Long estudianteId) {
        return competenciasEstudiante.findByEstudianteId(estudianteId);
    }

    public PerfilEstudianteDto dto(Estudiante e) {
        HojaVidaDto hoja = hojasVida.findFirstByEstudianteIdAndActivaTrue(e.getId()).map(HojaVidaDto::de).orElse(null);
        List<CompetenciaEstudianteDto> comps = competenciasEstudiante.findByEstudianteId(e.getId()).stream()
                .map(CompetenciaEstudianteDto::de).toList();
        List<PortafolioDto> port = portafolios.findByEstudianteIdOrderByIdAsc(e.getId()).stream()
                .map(PortafolioDto::de).toList();
        return new PerfilEstudianteDto(e.getId(), e.getUsuario().getCorreo(), e.getDocumento(), e.getNombres(),
                e.getApellidos(), e.getPrograma(), e.getSemestre(), e.getMunicipio(), e.getDisponibilidad(),
                e.getEstado(), e.getMotivoRechazo(), hoja, comps, port,
                e.getEstado() == EstadoEstudiante.VALIDADO && hoja != null);
    }

    public record HojaVidaArchivo(Resource archivo, String nombreOriginal) {
    }

    // ---------- Utilidades ----------

    /** Si el registro fue rechazado y el estudiante corrige sus datos, vuelve a revisión. */
    private void volverAPendienteSiFueRechazado(Estudiante e, Long usuarioId) {
        if (e.getEstado() == EstadoEstudiante.RECHAZADO) {
            e.setEstado(EstadoEstudiante.PENDIENTE);
            e.setMotivoRechazo(null);
            actividad.registrar(usuarioId, "REENVIAR_VALIDACION", "estudiante", e.getId(), "Datos corregidos");
        }
    }

    private Portafolio portafolioPropio(Long usuarioId, Long id) {
        return portafolios.findByIdAndEstudianteId(id, delUsuario(usuarioId).getId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El elemento del portafolio no existe."));
    }

    private static void aplicar(Portafolio p, PortafolioRequest datos) {
        p.setTipo(datos.tipo());
        p.setTitulo(datos.titulo().trim());
        p.setUrl(Textos.limpiar(datos.url()));
        p.setDescripcion(Textos.limpiar(datos.descripcion()));
    }

    private static String nombreOriginal(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            return "hoja-de-vida.pdf";
        }
        // Solo se conserva para mostrarlo; se quitan rutas y caracteres extraños.
        nombre = nombre.replaceAll("^.*[\\\\/]", "").replaceAll("[^\\p{L}\\p{N} ._()-]", "_");
        return nombre.length() > 200 ? nombre.substring(nombre.length() - 200) : nombre;
    }
}
