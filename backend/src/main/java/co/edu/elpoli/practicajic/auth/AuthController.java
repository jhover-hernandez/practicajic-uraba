package co.edu.elpoli.practicajic.auth;

import co.edu.elpoli.practicajic.auth.dto.*;
import co.edu.elpoli.practicajic.seguridad.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y recuperación de contraseña (RF-01 a RF-03)")
public class AuthController {

    static final String COOKIE_REFRESH = "pj_refresh";

    private final AuthService authService;
    private final boolean cookieSegura;

    public AuthController(AuthService authService, @Value("${app.cookie-segura}") boolean cookieSegura) {
        this.authService = authService;
        this.cookieSegura = cookieSegura;
    }

    @PostMapping("/registro/estudiante")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta de estudiante (queda Pendiente de validación)")
    public UsuarioResponse registrarEstudiante(@Valid @RequestBody RegistroEstudianteRequest datos) {
        return authService.registrarEstudiante(datos);
    }

    @PostMapping("/registro/empresa")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta de empresa (queda Pendiente de validación)")
    public UsuarioResponse registrarEmpresa(@Valid @RequestBody RegistroEmpresaRequest datos) {
        return authService.registrarEmpresa(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión. Devuelve el token de acceso y deja el de renovación en una cookie HttpOnly")
    public ResponseEntity<SesionResponse> login(@Valid @RequestBody LoginRequest datos) {
        return conCookie(authService.iniciarSesion(datos));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token de acceso usando la cookie de renovación")
    public ResponseEntity<SesionResponse> renovar(@CookieValue(name = COOKIE_REFRESH, required = false) String refresh) {
        return conCookie(authService.renovar(refresh));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE_REFRESH, required = false) String refresh) {
        authService.cerrarSesion(refresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    @PostMapping("/recuperar")
    @Operation(summary = "Solicitar enlace de recuperación de contraseña por correo")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest datos) {
        authService.solicitarRecuperacion(datos);
        return new MensajeResponse(
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.");
    }

    @PostMapping("/restablecer")
    @Operation(summary = "Definir una nueva contraseña con el enlace de recuperación")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerRequest datos) {
        authService.restablecerContrasena(datos);
        return new MensajeResponse("Tu contraseña se actualizó. Ya puedes iniciar sesión.");
    }

    @GetMapping("/yo")
    @Operation(summary = "Datos del usuario en sesión")
    public UsuarioResponse yo(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return authService.usuarioActual(actual.id());
    }

    private ResponseEntity<SesionResponse> conCookie(AuthService.SesionEmitida emitida) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie(emitida.refreshToken(), emitida.duracionRefresh()).toString())
                .body(emitida.sesion());
    }

    /** Cookie solo accesible por el servidor y limitada a las rutas de autenticación. */
    private ResponseCookie cookie(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE_REFRESH, valor)
                .httpOnly(true)
                .secure(cookieSegura)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(duracion)
                .build();
    }
}
