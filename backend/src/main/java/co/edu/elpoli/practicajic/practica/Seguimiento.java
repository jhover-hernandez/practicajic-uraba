package co.edu.elpoli.practicajic.practica;

import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-36 y RF-37: visita, informe, evaluación parcial o evaluación final de la práctica. */
@Entity
@Table(name = "seguimiento")
public class Seguimiento {

    public enum Tipo {
        VISITA,
        INFORME,
        EVALUACION_PARCIAL,
        EVALUACION_FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(precision = 3, scale = 1)
    private BigDecimal calificacion;

    protected Seguimiento() {
    }

    public Seguimiento(Practica practica, Usuario responsable, LocalDate fecha, Tipo tipo, String descripcion,
                       BigDecimal calificacion) {
        this.practica = practica;
        this.responsable = responsable;
        this.fecha = fecha;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.calificacion = calificacion;
    }

    public Long getId() { return id; }
    public Practica getPractica() { return practica; }
    public Usuario getResponsable() { return responsable; }
    public LocalDate getFecha() { return fecha; }
    public Tipo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getCalificacion() { return calificacion; }
}
