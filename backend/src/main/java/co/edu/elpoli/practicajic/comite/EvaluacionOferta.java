package co.edu.elpoli.practicajic.comite;

import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** RF-29 y RF-30: matriz de evaluación de empresas (25 + 20 + 20 + 15 + 10 + 10 = 100) y decisión. */
@Entity
@Table(name = "evaluacion_oferta")
public class EvaluacionOferta {

    public enum Decision {
        APROBAR,
        AJUSTES,
        RECHAZAR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaPractica oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Usuario evaluador;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pertinencia;

    @Column(name = "calidad_plan", nullable = false, precision = 5, scale = 2)
    private BigDecimal calidadPlan;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal condiciones;

    @Column(name = "incentivo_economico", nullable = false, precision = 5, scale = 2)
    private BigDecimal incentivoEconomico;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal acompanamiento;

    @Column(name = "experiencia_laboral", nullable = false, precision = 5, scale = 2)
    private BigDecimal experienciaLaboral;

    @Column(name = "puntaje_total", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Decision decision;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected EvaluacionOferta() {
    }

    public EvaluacionOferta(OfertaPractica oferta, Usuario evaluador, BigDecimal pertinencia, BigDecimal calidadPlan,
                            BigDecimal condiciones, BigDecimal incentivoEconomico, BigDecimal acompanamiento,
                            BigDecimal experienciaLaboral, Decision decision, String observaciones) {
        this.oferta = oferta;
        this.evaluador = evaluador;
        this.pertinencia = pertinencia;
        this.calidadPlan = calidadPlan;
        this.condiciones = condiciones;
        this.incentivoEconomico = incentivoEconomico;
        this.acompanamiento = acompanamiento;
        this.experienciaLaboral = experienciaLaboral;
        this.puntajeTotal = pertinencia.add(calidadPlan).add(condiciones).add(incentivoEconomico)
                .add(acompanamiento).add(experienciaLaboral);
        this.decision = decision;
        this.observaciones = observaciones;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public OfertaPractica getOferta() { return oferta; }
    public Usuario getEvaluador() { return evaluador; }
    public BigDecimal getPertinencia() { return pertinencia; }
    public BigDecimal getCalidadPlan() { return calidadPlan; }
    public BigDecimal getCondiciones() { return condiciones; }
    public BigDecimal getIncentivoEconomico() { return incentivoEconomico; }
    public BigDecimal getAcompanamiento() { return acompanamiento; }
    public BigDecimal getExperienciaLaboral() { return experienciaLaboral; }
    public BigDecimal getPuntajeTotal() { return puntajeTotal; }
    public Decision getDecision() { return decision; }
    public String getObservaciones() { return observaciones; }
    public LocalDateTime getFecha() { return fecha; }
}
