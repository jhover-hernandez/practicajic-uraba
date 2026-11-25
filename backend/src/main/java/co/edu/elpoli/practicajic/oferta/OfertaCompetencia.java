package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.catalogo.Competencia;
import co.edu.elpoli.practicajic.catalogo.Nivel;
import jakarta.persistence.*;

/** Competencia que pide la oferta y el nivel requerido (RF-08). */
@Entity
@Table(name = "oferta_competencia")
public class OfertaCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaPractica oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_requerido", nullable = false, length = 15)
    private Nivel nivelRequerido;

    protected OfertaCompetencia() {
    }

    public OfertaCompetencia(OfertaPractica oferta, Competencia competencia, Nivel nivelRequerido) {
        this.oferta = oferta;
        this.competencia = competencia;
        this.nivelRequerido = nivelRequerido;
    }

    public Long getId() { return id; }
    public OfertaPractica getOferta() { return oferta; }
    public Competencia getCompetencia() { return competencia; }
    public Nivel getNivelRequerido() { return nivelRequerido; }
}
