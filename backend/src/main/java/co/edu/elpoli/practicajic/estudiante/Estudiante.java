package co.edu.elpoli.practicajic.estudiante;

import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

/**
 * Perfil básico que se crea al registrar la cuenta (RF-01).
 * El módulo de estudiantes (Tarea 11) completa hoja de vida, competencias y portafolio.
 */
@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 120)
    private String programa;

    @Column(nullable = false)
    private Short semestre;

    @Column(length = 60)
    private String municipio;

    @Column(length = 60)
    private String disponibilidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEstudiante estado = EstadoEstudiante.PENDIENTE;

    protected Estudiante() {
    }

    public Estudiante(Usuario usuario, String documento, String nombres, String apellidos,
                      String programa, Short semestre, String municipio) {
        this.usuario = usuario;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.programa = programa;
        this.semestre = semestre;
        this.municipio = municipio;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getPrograma() { return programa; }
    public void setPrograma(String programa) { this.programa = programa; }
    public Short getSemestre() { return semestre; }
    public void setSemestre(Short semestre) { this.semestre = semestre; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }
    public EstadoEstudiante getEstado() { return estado; }
    public void setEstado(EstadoEstudiante estado) { this.estado = estado; }
}
