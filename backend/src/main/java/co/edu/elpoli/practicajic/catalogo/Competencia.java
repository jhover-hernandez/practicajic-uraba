package co.edu.elpoli.practicajic.catalogo;

import jakarta.persistence.*;

/** Catálogo de competencias técnicas y blandas (RF-14, RF-21). */
@Entity
@Table(name = "competencia")
public class Competencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoriaCompetencia categoria;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    protected Competencia() {
    }

    public Competencia(String nombre, CategoriaCompetencia categoria, String descripcion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public CategoriaCompetencia getCategoria() { return categoria; }
    public void setCategoria(CategoriaCompetencia categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
