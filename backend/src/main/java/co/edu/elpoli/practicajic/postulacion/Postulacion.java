package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.oferta.OfertaPractica;
import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Solicitud de un estudiante a una convocatoria (RF-22). RN-06: única por oferta y estudiante. */
@Entity
@Table(name = "postulacion")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaPractica oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoPostulacion estado = EstadoPostulacion.POSTULADO;

    /** RF-25: cuántas competencias pedidas por la oferta tenía el estudiante al postularse. */
    @Column(name = "competencias_coincidentes", nullable = false)
    private Short competenciasCoincidentes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id")
    private Usuario evaluador;

    @Column(name = "fecha_entrevista")
    private LocalDateTime fechaEntrevista;

    @Column(name = "lugar_entrevista", length = 200)
    private String lugarEntrevista;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;

    protected Postulacion() {
    }

    public Postulacion(OfertaPractica oferta, Estudiante estudiante, short competenciasCoincidentes) {
        this.oferta = oferta;
        this.estudiante = estudiante;
        this.competenciasCoincidentes = competenciasCoincidentes;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public OfertaPractica getOferta() { return oferta; }
    public Estudiante getEstudiante() { return estudiante; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoPostulacion getEstado() { return estado; }
    public void setEstado(EstadoPostulacion estado) { this.estado = estado; }
    public Short getCompetenciasCoincidentes() { return competenciasCoincidentes; }
    public void setCompetenciasCoincidentes(Short n) { this.competenciasCoincidentes = n; }
    public Usuario getEvaluador() { return evaluador; }
    public void setEvaluador(Usuario evaluador) { this.evaluador = evaluador; }
    public LocalDateTime getFechaEntrevista() { return fechaEntrevista; }
    public void setFechaEntrevista(LocalDateTime fechaEntrevista) { this.fechaEntrevista = fechaEntrevista; }
    public String getLugarEntrevista() { return lugarEntrevista; }
    public void setLugarEntrevista(String lugarEntrevista) { this.lugarEntrevista = lugarEntrevista; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaAceptacion() { return fechaAceptacion; }
    public void setFechaAceptacion(LocalDateTime fechaAceptacion) { this.fechaAceptacion = fechaAceptacion; }
}
