package co.edu.elpoli.practicajic.panel;

import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.empresa.EmpresaRepository;
import co.edu.elpoli.practicajic.estudiante.EstudianteRepository;
import co.edu.elpoli.practicajic.oferta.OfertaRepository;
import co.edu.elpoli.practicajic.postulacion.PostulacionRepository;
import co.edu.elpoli.practicajic.practica.PracticaRepository;
import co.edu.elpoli.practicajic.practica.Seguimiento;
import co.edu.elpoli.practicajic.practica.SeguimientoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RF-40: informes en CSV que Excel abre directamente (UTF-8 con BOM y punto y coma, como lo espera
 * Excel en español). El PDF se genera desde el navegador con la vista de impresión del panel.
 */
@Service
@Transactional(readOnly = true)
public class InformeService {

    public static final List<String> TIPOS = List.of("empresas", "estudiantes", "convocatorias", "postulaciones", "practicas");

    private final EmpresaRepository empresas;
    private final EstudianteRepository estudiantes;
    private final OfertaRepository ofertas;
    private final PostulacionRepository postulaciones;
    private final PracticaRepository practicas;
    private final SeguimientoRepository seguimientos;

    public InformeService(EmpresaRepository empresas, EstudianteRepository estudiantes, OfertaRepository ofertas,
                          PostulacionRepository postulaciones, PracticaRepository practicas,
                          SeguimientoRepository seguimientos) {
        this.empresas = empresas;
        this.estudiantes = estudiantes;
        this.ofertas = ofertas;
        this.postulaciones = postulaciones;
        this.practicas = practicas;
        this.seguimientos = seguimientos;
    }

    public byte[] csv(String tipo, String textoPeriodo) {
        Periodo periodo = Periodo.de(textoPeriodo);
        List<List<Object>> filas = new ArrayList<>();
        switch (tipo) {
            case "empresas" -> {
                filas.add(List.of("NIT", "Razón social", "Nombre comercial", "Sector", "Tamaño", "Municipio",
                        "Contacto", "Correo", "Estado", "Fecha de registro"));
                empresas.findAllByOrderByRazonSocialAsc().stream()
                        .filter(e -> periodo.existiaAlCierre(e.getUsuario().getFechaCreacion()))
                        .forEach(e -> filas.add(fila(e.getNit(), e.getRazonSocial(), e.getNombreComercial(), e.getSector(),
                                e.getTamano(), e.getMunicipio(), e.getContacto(), e.getUsuario().getCorreo(),
                                e.getEstadoValidacion(), e.getUsuario().getFechaCreacion().toLocalDate())));
            }
            case "estudiantes" -> {
                filas.add(List.of("Documento", "Nombres", "Apellidos", "Correo", "Programa", "Semestre", "Municipio",
                        "Disponibilidad", "Estado", "Fecha de registro"));
                estudiantes.findAllByOrderByApellidosAscNombresAsc().stream()
                        .filter(e -> periodo.existiaAlCierre(e.getUsuario().getFechaCreacion()))
                        .forEach(e -> filas.add(fila(e.getDocumento(), e.getNombres(), e.getApellidos(),
                                e.getUsuario().getCorreo(), e.getPrograma(), e.getSemestre(), e.getMunicipio(),
                                e.getDisponibilidad(), e.getEstado(), e.getUsuario().getFechaCreacion().toLocalDate())));
            }
            case "convocatorias" -> {
                filas.add(List.of("Título", "Empresa", "Área", "Modalidad", "Municipio", "Duración (meses)", "Vacantes",
                        "Incentivo", "Vinculación", "Estado", "Publicación", "Cierre", "Postulados"));
                ofertas.findAll().stream()
                        .filter(o -> periodo.contiene(o.getFechaCreacion()))
                        .forEach(o -> filas.add(fila(o.getTitulo(), o.getEmpresa().getNombreVisible(), o.getArea().getNombre(),
                                o.getModalidad(), o.getMunicipio(), o.getDuracionMeses(), o.getVacantes(), o.getIncentivo(),
                                o.getTipoVinculacion(), o.getEstado(), o.getFechaPublicacion(), o.getFechaCierre(),
                                postulaciones.countByOfertaId(o.getId()))));
            }
            case "postulaciones" -> {
                filas.add(List.of("Fecha", "Estudiante", "Documento", "Programa", "Convocatoria", "Empresa", "Estado",
                        "Competencias coincidentes"));
                postulaciones.findAll().stream()
                        .filter(p -> periodo.contiene(p.getFecha()))
                        .forEach(p -> filas.add(fila(p.getFecha().toLocalDate(), p.getEstudiante().getNombreCompleto(),
                                p.getEstudiante().getDocumento(), p.getEstudiante().getPrograma(), p.getOferta().getTitulo(),
                                p.getOferta().getEmpresa().getNombreVisible(), p.getEstado(),
                                p.getCompetenciasCoincidentes() + " de " + p.getOferta().getCompetencias().size())));
            }
            case "practicas" -> {
                filas.add(List.of("Estudiante", "Documento", "Empresa", "Convocatoria", "Inicio", "Fin", "Vinculación",
                        "Incentivo", "Tutor empresa", "Tutor Politécnico", "Estado", "Seguimientos", "Calificación final"));
                practicas.findAllByOrderByFechaInicioDesc().stream()
                        .filter(p -> periodo.contiene(p.getFechaInicio()))
                        .forEach(p -> {
                            var lista = seguimientos.findByPracticaIdOrderByFechaAscIdAsc(p.getId());
                            Object nota = lista.stream().filter(s -> s.getTipo() == Seguimiento.Tipo.EVALUACION_FINAL)
                                    .map(Seguimiento::getCalificacion).findFirst().orElse(null);
                            var post = p.getPostulacion();
                            filas.add(fila(post.getEstudiante().getNombreCompleto(), post.getEstudiante().getDocumento(),
                                    post.getOferta().getEmpresa().getNombreVisible(), post.getOferta().getTitulo(),
                                    p.getFechaInicio(), p.getFechaFin(), p.getTipoVinculacion(), p.getIncentivo(),
                                    p.getTutorEmpresa(), p.getTutorInstitucion(), p.getEstado(), lista.size(), nota));
                        });
            }
            default -> throw new NegocioException(HttpStatus.NOT_FOUND,
                    "El informe no existe. Usa uno de estos: " + String.join(", ", TIPOS) + ".");
        }
        return aCsv(filas);
    }

    private static List<Object> fila(Object... valores) {
        List<Object> fila = new ArrayList<>(valores.length);
        for (Object v : valores) {
            fila.add(v);
        }
        return fila;
    }

    private static byte[] aCsv(List<List<Object>> filas) {
        StringBuilder sb = new StringBuilder("﻿");
        for (List<Object> fila : filas) {
            for (int i = 0; i < fila.size(); i++) {
                if (i > 0) {
                    sb.append(';');
                }
                sb.append(celda(fila.get(i)));
            }
            sb.append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String celda(Object valor) {
        String texto = Objects.toString(valor, "");
        // Evita que Excel interprete el contenido como fórmula (inyección CSV).
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
