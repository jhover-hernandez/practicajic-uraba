package co.edu.elpoli.practicajic.usuario;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash BCrypt (RNF-01). La contraseña nunca se guarda en texto plano. */
    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(length = 150)
    private String nombre;

    @Column(name = "fecha_aceptacion_politica")
    private LocalDateTime fechaAceptacionPolitica;

    protected Usuario() {
    }

    public Usuario(String correo, String contrasenaHash, Rol rol, String nombre) {
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.nombre = nombre;
    }

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDateTime getFechaAceptacionPolitica() { return fechaAceptacionPolitica; }
    public void setFechaAceptacionPolitica(LocalDateTime fecha) { this.fechaAceptacionPolitica = fecha; }
}
