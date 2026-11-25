package co.edu.elpoli.practicajic.config;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.AuthService;
import co.edu.elpoli.practicajic.usuario.Rol;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea el primer administrador al arrancar, porque nadie más puede crear cuentas de ADMIN. */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final ActividadService actividad;
    private final String correo;
    private final String contrasena;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder codificador, ActividadService actividad,
                        @Value("${app.admin.correo}") String correo,
                        @Value("${app.admin.contrasena}") String contrasena) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.actividad = actividad;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRol(Rol.ADMIN)) {
            return;
        }
        if (contrasena == null || contrasena.isBlank()) {
            log.warn("No hay ningún administrador. Define APP_ADMIN_CORREO y APP_ADMIN_CONTRASENA para crear el primero.");
            return;
        }
        Usuario admin = usuarios.save(new Usuario(AuthService.normalizarCorreo(correo), codificador.encode(contrasena),
                Rol.ADMIN, "Administrador institucional"));
        actividad.registrar(null, "CREAR_USUARIO", "usuario", admin.getId(), "Administrador inicial");
        log.info("Administrador inicial creado: {}", admin.getCorreo());
    }
}
