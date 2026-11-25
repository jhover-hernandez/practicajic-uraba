package co.edu.elpoli.practicajic.panel;

import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.EmpresaRepository;
import co.edu.elpoli.practicajic.empresa.EstadoValidacionEmpresa;
import co.edu.elpoli.practicajic.estudiante.EstadoEstudiante;
import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.estudiante.EstudianteRepository;
import co.edu.elpoli.practicajic.oferta.EstadoOferta;
import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.oferta.OfertaRepository;
import co.edu.elpoli.practicajic.panel.PanelDtos.Conteo;
import co.edu.elpoli.practicajic.panel.PanelDtos.IndicadoresDto;
import co.edu.elpoli.practicajic.postulacion.EstadoPostulacion;
import co.edu.elpoli.practicajic.postulacion.Postulacion;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import co.edu.elpoli.practicajic.practica.EstadoPractica;
import co.edu.elpoli.practicajic.practica.Practica;
import co.edu.elpoli.practicajic.practica.PracticaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RF-38 y RF-39. El piloto maneja pocos cientos de registros, así que se calcula en memoria;
 * si el volumen crece, estas cuentas se pasan a consultas agregadas.
 */
@Service
@Transactional(readOnly = true)
public class PanelService {

    private final EmpresaRepository empresas;
    private final EstudianteRepository estudiantes;
    private final OfertaRepository ofertas;
    private final PostulacionRepository postulaciones;
    private final PracticaRepository practicas;

    public PanelService(EmpresaRepository empresas, EstudianteRepository estudiantes, OfertaRepository ofertas,
                        PostulacionRepository postulaciones, PracticaRepository practicas) {
        this.empresas = empresas;
        this.estudiantes = estudiantes;
        this.ofertas = ofertas;
        this.postulaciones = postulaciones;
        this.practicas = practicas;
    }

    public IndicadoresDto indicadores(String textoPeriodo) {
        Periodo periodo = Periodo.de(textoPeriodo);

        List<Empresa> listaEmpresas = empresas.findAll().stream()
                .filter(e -> periodo.existiaAlCierre(e.getUsuario().getFechaCreacion())).toList();
        List<Estudiante> listaEstudiantes = estudiantes.findAll().stream()
                .filter(e -> periodo.existiaAlCierre(e.getUsuario().getFechaCreacion())).toList();
        List<OfertaPractica> listaOfertas = ofertas.findAll().stream()
                .filter(o -> periodo.contiene(o.getFechaCreacion())).toList();
        List<Postulacion> listaPostulaciones = postulaciones.findAll().stream()
                .filter(p -> periodo.contiene(p.getFecha())).toList();
        List<Practica> listaPracticas = practicas.findAll().stream()
                .filter(p -> periodo.contiene(p.getFechaInicio())).toList();

        List<Empresa> validadas = listaEmpresas.stream()
                .filter(e -> e.getEstadoValidacion() == EstadoValidacionEmpresa.VALIDADA).toList();

        // Tasa de colocación: estudiantes con práctica frente a estudiantes que se postularon.
        Set<Long> postulados = listaPostulaciones.stream().map(p -> p.getEstudiante().getId()).collect(Collectors.toSet());
        Set<Long> conPractica = listaPracticas.stream()
                .map(p -> p.getPostulacion().getEstudiante().getId()).collect(Collectors.toSet());
        BigDecimal tasa = postulados.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(conPractica.size() * 100L).divide(BigDecimal.valueOf(postulados.size()), 1, RoundingMode.HALF_UP);

        long porFormalizar = listaPostulaciones.stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.SELECCIONADO && p.getFechaAceptacion() != null).count();

        return new IndicadoresDto(periodo.nombre(), periodo.nombre() == null ? null : periodo.desde(),
                periodo.nombre() == null ? null : periodo.hasta(), Periodo.disponibles(),
                validadas.size(),
                listaEstudiantes.stream().filter(e -> e.getEstado() == EstadoEstudiante.VALIDADO).count(),
                listaOfertas.stream().filter(OfertaPractica::estaAbierta).count(),
                listaPostulaciones.size(),
                listaPracticas.size(),
                listaPracticas.stream().filter(p -> p.getEstado() == EstadoPractica.FINALIZADA).count(),
                tasa, postulados.size(), conPractica.size(),
                listaEmpresas.stream().filter(e -> e.getEstadoValidacion() == EstadoValidacionEmpresa.PENDIENTE).count(),
                listaEstudiantes.stream().filter(e -> e.getEstado() == EstadoEstudiante.PENDIENTE).count(),
                listaOfertas.stream().filter(o -> o.getEstado() == EstadoOferta.EN_REVISION).count(),
                listaOfertas.stream().filter(o -> o.getEstado() == EstadoOferta.EN_REVISION && o.getEvaluador() == null).count(),
                porFormalizar,
                contar(validadas, Empresa::getMunicipio),
                contar(listaOfertas.stream().filter(o -> o.getEstado() != EstadoOferta.BORRADOR).toList(),
                        o -> o.getArea().getNombre()),
                contar(listaPostulaciones, p -> p.getEstado().name()));
    }

    /** Agrupa y ordena de mayor a menor. */
    static <T> List<Conteo> contar(Collection<T> elementos, Function<T, String> clave) {
        return elementos.stream()
                .collect(Collectors.groupingBy(e -> Objects.requireNonNullElse(clave.apply(e), "Sin dato"), Collectors.counting()))
                .entrySet().stream()
                .map(en -> new Conteo(en.getKey(), en.getValue()))
                .sorted(Comparator.comparingLong(Conteo::cantidad).reversed().thenComparing(Conteo::etiqueta))
                .toList();
    }
}
