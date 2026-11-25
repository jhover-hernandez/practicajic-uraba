package co.edu.elpoli.practicajic.catalogo;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.catalogo.CatalogoDtos.*;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-21: el administrador gestiona las áreas y el catálogo de competencias. */
@Service
@Transactional
public class CatalogoService {

    private final AreaRepository areas;
    private final CompetenciaRepository competencias;
    private final ActividadService actividad;

    public CatalogoService(AreaRepository areas, CompetenciaRepository competencias, ActividadService actividad) {
        this.areas = areas;
        this.competencias = competencias;
        this.actividad = actividad;
    }

    @Transactional(readOnly = true)
    public List<AreaDto> areas() {
        return areas.findAllByOrderByNombreAsc().stream().map(AreaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<CompetenciaDto> competencias() {
        return competencias.findAllByOrderByCategoriaAscNombreAsc().stream().map(CompetenciaDto::de).toList();
    }

    public AreaDto crearArea(AreaRequest datos, Long actorId) {
        String nombre = datos.nombre().trim();
        if (areas.existsByNombreIgnoreCase(nombre)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe un área con ese nombre.");
        }
        Area area = areas.save(new Area(nombre));
        actividad.registrar(actorId, "CREAR_AREA", "area", area.getId(), nombre);
        return AreaDto.de(area);
    }

    public AreaDto editarArea(Long id, AreaRequest datos, Long actorId) {
        Area area = buscarArea(id);
        String nombre = datos.nombre().trim();
        if (!nombre.equalsIgnoreCase(area.getNombre()) && areas.existsByNombreIgnoreCase(nombre)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe un área con ese nombre.");
        }
        area.setNombre(nombre);
        actividad.registrar(actorId, "EDITAR_AREA", "area", id, nombre);
        return AreaDto.de(area);
    }

    public void eliminarArea(Long id, Long actorId) {
        Area area = buscarArea(id);
        try {
            areas.delete(area);
            areas.flush();
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException(HttpStatus.CONFLICT, "No se puede eliminar: hay ofertas que usan esta área.");
        }
        actividad.registrar(actorId, "ELIMINAR_AREA", "area", id, area.getNombre());
    }

    public CompetenciaDto crearCompetencia(CompetenciaRequest datos, Long actorId) {
        String nombre = datos.nombre().trim();
        if (competencias.existsByNombreIgnoreCase(nombre)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una competencia con ese nombre.");
        }
        Competencia c = competencias.save(new Competencia(nombre, datos.categoria(), Textos.limpiar(datos.descripcion())));
        actividad.registrar(actorId, "CREAR_COMPETENCIA", "competencia", c.getId(), nombre);
        return CompetenciaDto.de(c);
    }

    public CompetenciaDto editarCompetencia(Long id, CompetenciaRequest datos, Long actorId) {
        Competencia c = buscarCompetencia(id);
        String nombre = datos.nombre().trim();
        if (!nombre.equalsIgnoreCase(c.getNombre()) && competencias.existsByNombreIgnoreCase(nombre)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una competencia con ese nombre.");
        }
        c.setNombre(nombre);
        c.setCategoria(datos.categoria());
        c.setDescripcion(Textos.limpiar(datos.descripcion()));
        actividad.registrar(actorId, "EDITAR_COMPETENCIA", "competencia", id, nombre);
        return CompetenciaDto.de(c);
    }

    public void eliminarCompetencia(Long id, Long actorId) {
        Competencia c = buscarCompetencia(id);
        try {
            competencias.delete(c);
            competencias.flush();
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException(HttpStatus.CONFLICT,
                    "No se puede eliminar: hay estudiantes u ofertas que usan esta competencia.");
        }
        actividad.registrar(actorId, "ELIMINAR_COMPETENCIA", "competencia", id, c.getNombre());
    }

    public Area buscarArea(Long id) {
        return areas.findById(id).orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El área no existe."));
    }

    public Competencia buscarCompetencia(Long id) {
        return competencias.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "La competencia no existe."));
    }
}
