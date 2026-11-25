package co.edu.elpoli.practicajic.practica;

import co.edu.elpoli.practicajic.postulacion.Postulacion;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-35: vinculación formal del estudiante seleccionado con la empresa. */
@Entity
@Table(name = "practica")
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postulacion_id", nullable = false, unique = true)
    private Postulacion postulacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "tipo_vinculacion", length = 60)
    private String tipoVinculacion;

    @Column(precision = 12, scale = 2)
    private BigDecimal incentivo;

    @Column(name = "tutor_empresa", length = 150)
    private String tutorEmpresa;

    @Column(name = "tutor_institucion", length = 150)
    private String tutorInstitucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPractica estado = EstadoPractica.INICIADA;

    protected Practica() {
    }

    public Practica(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public Long getId() { return id; }
    public Postulacion getPostulacion() { return postulacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getTipoVinculacion() { return tipoVinculacion; }
    public void setTipoVinculacion(String tipoVinculacion) { this.tipoVinculacion = tipoVinculacion; }
    public BigDecimal getIncentivo() { return incentivo; }
    public void setIncentivo(BigDecimal incentivo) { this.incentivo = incentivo; }
    public String getTutorEmpresa() { return tutorEmpresa; }
    public void setTutorEmpresa(String tutorEmpresa) { this.tutorEmpresa = tutorEmpresa; }
    public String getTutorInstitucion() { return tutorInstitucion; }
    public void setTutorInstitucion(String tutorInstitucion) { this.tutorInstitucion = tutorInstitucion; }
    public EstadoPractica getEstado() { return estado; }
    public void setEstado(EstadoPractica estado) { this.estado = estado; }
}
