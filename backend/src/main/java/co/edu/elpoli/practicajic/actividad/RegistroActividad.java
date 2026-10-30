package co.edu.elpoli.practicajic.actividad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** RNF-05: quién hizo qué y cuándo. */
@Entity
@Table(name = "registro_actividad")
public class RegistroActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected RegistroActividad() {
    }

    public RegistroActividad(Long usuarioId, String accion, String entidad, Long entidadId, String detalle) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public Long getEntidadId() { return entidadId; }
    public String getDetalle() { return detalle; }
    public LocalDateTime getFecha() { return fecha; }
}
