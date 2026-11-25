package co.edu.elpoli.practicajic.oferta;

import co.edu.elpoli.practicajic.catalogo.Area;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Necesidad de práctica publicada por una empresa (RF-08, RF-09). Aprobada por el comité es una convocatoria. */
@Entity
@Table(name = "oferta_practica")
public class OfertaPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String actividades;

    @Column(columnDefinition = "TEXT")
    private String perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Modalidad modalidad;

    @Column(nullable = false, length = 60)
    private String municipio;

    @Column(name = "duracion_meses", nullable = false)
    private Short duracionMeses;

    @Column(nullable = false)
    private Short vacantes;

    @Column(precision = 12, scale = 2)
    private BigDecimal incentivo;

    @Column(name = "tipo_vinculacion", length = 60)
    private String tipoVinculacion;

    @Column(length = 100)
    private String horario;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoOferta estado = EstadoOferta.BORRADOR;

    @Column(name = "observaciones_comite", columnDefinition = "TEXT")
    private String observacionesComite;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Miembro del comité asignado por el administrador (RF-34). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id")
    private Usuario evaluador;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OfertaCompetencia> competencias = new ArrayList<>();

    protected OfertaPractica() {
    }

    public OfertaPractica(Empresa empresa) {
        this.empresa = empresa;
    }

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /** RN-07: después de la fecha de cierre no se reciben postulaciones aunque el cierre automático no haya corrido. */
    public boolean estaAbierta() {
        return estado == EstadoOferta.APROBADA && (fechaCierre == null || !fechaCierre.isBefore(LocalDate.now()));
    }

    public Long getId() { return id; }
    public Empresa getEmpresa() { return empresa; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getActividades() { return actividades; }
    public void setActividades(String actividades) { this.actividades = actividades; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public Short getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(Short duracionMeses) { this.duracionMeses = duracionMeses; }
    public Short getVacantes() { return vacantes; }
    public void setVacantes(Short vacantes) { this.vacantes = vacantes; }
    public BigDecimal getIncentivo() { return incentivo; }
    public void setIncentivo(BigDecimal incentivo) { this.incentivo = incentivo; }
    public String getTipoVinculacion() { return tipoVinculacion; }
    public void setTipoVinculacion(String tipoVinculacion) { this.tipoVinculacion = tipoVinculacion; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }
    public EstadoOferta getEstado() { return estado; }
    public void setEstado(EstadoOferta estado) { this.estado = estado; }
    public String getObservacionesComite() { return observacionesComite; }
    public void setObservacionesComite(String observaciones) { this.observacionesComite = observaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public Usuario getEvaluador() { return evaluador; }
    public void setEvaluador(Usuario evaluador) { this.evaluador = evaluador; }
    public List<OfertaCompetencia> getCompetencias() { return competencias; }
}
