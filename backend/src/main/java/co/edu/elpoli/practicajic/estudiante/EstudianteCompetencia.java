package co.edu.elpoli.practicajic.estudiante;

import co.edu.elpoli.practicajic.catalogo.Competencia;
import co.edu.elpoli.practicajic.catalogo.Nivel;
import jakarta.persistence.*;

/** RF-14: competencia que declara el estudiante, con su nivel. */
@Entity
@Table(name = "estudiante_competencia")
public class EstudianteCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Nivel nivel;

    protected EstudianteCompetencia() {
    }

    public EstudianteCompetencia(Estudiante estudiante, Competencia competencia, Nivel nivel) {
        this.estudiante = estudiante;
        this.competencia = competencia;
        this.nivel = nivel;
    }

    public Long getId() { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public Competencia getCompetencia() { return competencia; }
    public Nivel getNivel() { return nivel; }
}
