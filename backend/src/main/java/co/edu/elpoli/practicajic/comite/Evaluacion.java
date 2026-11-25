package co.edu.elpoli.practicajic.comite;

import co.edu.elpoli.practicajic.postulacion.Postulacion;
import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** RF-31 y RF-32: matriz de evaluación del estudiante (30 + 25 + 15 + 15 + 10 + 5 = 100), concepto y recomendación. */
@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postulacion_id", nullable = false)
    private Postulacion postulacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Usuario evaluador;

    @Column(name = "puntaje_tecnico", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeTecnico;

    @Column(name = "puntaje_perfil", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajePerfil;

    @Column(name = "puntaje_academico", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeAcademico;

    @Column(name = "puntaje_portafolio", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajePortafolio;

    @Column(name = "puntaje_blandas", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeBlandas;

    @Column(name = "puntaje_entrevista", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeEntrevista;

    @Column(name = "puntaje_final", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeFinal;

    @Column(columnDefinition = "TEXT")
    private String concepto;

    @Column(nullable = false)
    private boolean recomendado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Evaluacion() {
    }

    public Evaluacion(Postulacion postulacion, Usuario evaluador) {
        this.postulacion = postulacion;
        this.evaluador = evaluador;
    }

    /** Fija los puntajes y recalcula el total. Un evaluador puede corregir su propia calificación. */
    public void calificar(BigDecimal tecnico, BigDecimal perfil, BigDecimal academico, BigDecimal portafolio,
                          BigDecimal blandas, BigDecimal entrevista, String concepto, boolean recomendado) {
        this.puntajeTecnico = tecnico;
        this.puntajePerfil = perfil;
        this.puntajeAcademico = academico;
        this.puntajePortafolio = portafolio;
        this.puntajeBlandas = blandas;
        this.puntajeEntrevista = entrevista;
        this.puntajeFinal = tecnico.add(perfil).add(academico).add(portafolio).add(blandas).add(entrevista);
        this.concepto = concepto;
        this.recomendado = recomendado;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Postulacion getPostulacion() { return postulacion; }
    public Usuario getEvaluador() { return evaluador; }
    public BigDecimal getPuntajeTecnico() { return puntajeTecnico; }
    public BigDecimal getPuntajePerfil() { return puntajePerfil; }
    public BigDecimal getPuntajeAcademico() { return puntajeAcademico; }
    public BigDecimal getPuntajePortafolio() { return puntajePortafolio; }
    public BigDecimal getPuntajeBlandas() { return puntajeBlandas; }
    public BigDecimal getPuntajeEntrevista() { return puntajeEntrevista; }
    public BigDecimal getPuntajeFinal() { return puntajeFinal; }
    public String getConcepto() { return concepto; }
    public boolean isRecomendado() { return recomendado; }
    public LocalDateTime getFecha() { return fecha; }
}
