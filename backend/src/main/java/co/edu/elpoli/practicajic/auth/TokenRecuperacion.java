package co.edu.elpoli.practicajic.auth;

import co.edu.elpoli.practicajic.usuario.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_recuperacion")
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expira;

    @Column(nullable = false)
    private boolean usado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected TokenRecuperacion() {
    }

    public TokenRecuperacion(Usuario usuario, String tokenHash, LocalDateTime expira) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expira = expira;
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(expira);
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public boolean isUsado() { return usado; }
    public void marcarUsado() { this.usado = true; }
}
