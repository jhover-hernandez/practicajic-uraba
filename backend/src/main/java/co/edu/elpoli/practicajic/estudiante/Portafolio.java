package co.edu.elpoli.practicajic.estudiante;

import jakarta.persistence.*;

/** RF-15: proyecto, repositorio o certificación del estudiante. */
@Entity
@Table(name = "portafolio")
public class Portafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 255)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    protected Portafolio() {
    }

    public Portafolio(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Long getId() { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
