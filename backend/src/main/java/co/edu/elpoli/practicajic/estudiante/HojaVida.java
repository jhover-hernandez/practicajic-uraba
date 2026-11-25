package co.edu.elpoli.practicajic.estudiante;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** RF-13: cada carga crea una versión nueva y deja inactivas las anteriores. */
@Entity
@Table(name = "hoja_vida")
public class HojaVida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    /** Nombre generado por el sistema (RNF-04). */
    @Column(nullable = false, length = 255)
    private String archivo;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "nombre_original", length = 255)
    private String nombreOriginal;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    protected HojaVida() {
    }

    public HojaVida(Estudiante estudiante, String archivo, int version, String nombreOriginal, long tamanoBytes) {
        this.estudiante = estudiante;
        this.archivo = archivo;
        this.version = version;
        this.nombreOriginal = nombreOriginal;
        this.tamanoBytes = tamanoBytes;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public String getArchivo() { return archivo; }
    public Integer getVersion() { return version; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public String getNombreOriginal() { return nombreOriginal; }
    public Long getTamanoBytes() { return tamanoBytes; }
}
