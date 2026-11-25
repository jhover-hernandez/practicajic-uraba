package co.edu.elpoli.practicajic.empresa;

import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

/** Perfil que se crea al registrar la cuenta (RF-01) y se completa en el perfil de empresa (RF-06). */
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 150)
    private String nombreComercial;

    @Column(length = 80)
    private String sector;

    @Column(length = 20)
    private String tamano;

    @Column(nullable = false, length = 60)
    private String municipio;

    @Column(length = 150)
    private String contacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoValidacionEmpresa estadoValidacion = EstadoValidacionEmpresa.PENDIENTE;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    protected Empresa() {
    }

    public Empresa(Usuario usuario, String nit, String razonSocial, String nombreComercial,
                   String municipio, String contacto) {
        this.usuario = usuario;
        this.nit = nit;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
        this.municipio = municipio;
        this.contacto = contacto;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public EstadoValidacionEmpresa getEstadoValidacion() { return estadoValidacion; }
    public void setEstadoValidacion(EstadoValidacionEmpresa estado) { this.estadoValidacion = estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    /** Nombre que se muestra a los estudiantes: el comercial si existe. */
    public String getNombreVisible() {
        return nombreComercial != null ? nombreComercial : razonSocial;
    }
}
