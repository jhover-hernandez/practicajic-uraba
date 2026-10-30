package co.edu.elpoli.practicajic.auth;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.auth.dto.*;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.empresa.Empresa;
import co.edu.elpoli.practicajic.empresa.EmpresaRepository;
import co.edu.elpoli.practicajic.estudiante.Estudiante;
import co.edu.elpoli.practicajic.estudiante.EstudianteRepository;
import co.edu.elpoli.practicajic.seguridad.IntentosLogin;
import co.edu.elpoli.practicajic.seguridad.JwtService;
import co.edu.elpoli.practicajic.seguridad.Tokens;
import co.edu.elpoli.practicajic.usuario.EstadoPerfil;
import co.edu.elpoli.practicajic.usuario.Rol;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

/** RF-01 a RF-03: registro, inicio y cierre de sesión, renovación y recuperación de contraseña. */
@Service
@Transactional
public class AuthService {

    /** Resultado interno de login/renovación: la sesión para el cuerpo y el refresh token para la cookie. */
    public record SesionEmitida(SesionResponse sesion, String refreshToken, Duration duracionRefresh) {
    }

    private static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos.";
    private static final String SESION_INVALIDA = "Tu sesión expiró. Inicia sesión de nuevo.";

    private final UsuarioRepository usuarios;
    private final EstudianteRepository estudiantes;
    private final EmpresaRepository empresas;
    private final RefreshTokenRepository refreshTokens;
    private final TokenRecuperacionRepository tokensRecuperacion;
    private final PasswordEncoder codificador;
    private final JwtService jwtService;
    private final IntentosLogin intentosLogin;
    private final CorreoService correoService;
    private final ActividadService actividad;
    private final EstadoPerfil estadoPerfil;
    private final Duration duracionRefresh;
    private final Duration duracionRecuperacion;
    private final String frontendUrl;
    /** Hash de referencia para comparar aunque el correo no exista y no delatar cuentas por el tiempo de respuesta. */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, EstudianteRepository estudiantes, EmpresaRepository empresas,
                       RefreshTokenRepository refreshTokens, TokenRecuperacionRepository tokensRecuperacion,
                       PasswordEncoder codificador, JwtService jwtService, IntentosLogin intentosLogin,
                       CorreoService correoService, ActividadService actividad, EstadoPerfil estadoPerfil,
                       @Value("${app.jwt.duracion-refresh}") Duration duracionRefresh,
                       @Value("${app.recuperacion.duracion}") Duration duracionRecuperacion,
                       @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarios = usuarios;
        this.estudiantes = estudiantes;
        this.empresas = empresas;
        this.refreshTokens = refreshTokens;
        this.tokensRecuperacion = tokensRecuperacion;
        this.codificador = codificador;
        this.jwtService = jwtService;
        this.intentosLogin = intentosLogin;
        this.correoService = correoService;
        this.actividad = actividad;
        this.estadoPerfil = estadoPerfil;
        this.duracionRefresh = duracionRefresh;
        this.duracionRecuperacion = duracionRecuperacion;
        this.frontendUrl = frontendUrl;
        this.hashFicticio = codificador.encode(Tokens.generar());
    }

    // ---------- Registro (RF-01) ----------

    public UsuarioResponse registrarEstudiante(RegistroEstudianteRequest datos) {
        String correo = normalizarCorreo(datos.correo());
        validarCorreoLibre(correo);
        if (estudiantes.existsByDocumento(datos.documento())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe un estudiante registrado con ese documento.");
        }
        String nombre = datos.nombres().trim() + " " + datos.apellidos().trim();
        Usuario usuario = crearUsuario(correo, datos.contrasena(), Rol.ESTUDIANTE, nombre);
        estudiantes.save(new Estudiante(usuario, datos.documento(), datos.nombres().trim(), datos.apellidos().trim(),
                datos.programa().trim(), datos.semestre(), vacioANulo(datos.municipio())));
        actividad.registrar(usuario.getId(), "REGISTRO", "usuario", usuario.getId(), "Registro de estudiante");
        return estadoPerfil.respuesta(usuario);
    }

    public UsuarioResponse registrarEmpresa(RegistroEmpresaRequest datos) {
        String correo = normalizarCorreo(datos.correo());
        validarCorreoLibre(correo);
        if (empresas.existsByNit(datos.nit())) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una empresa registrada con ese NIT.");
        }
        Usuario usuario = crearUsuario(correo, datos.contrasena(), Rol.EMPRESA, datos.razonSocial().trim());
        empresas.save(new Empresa(usuario, datos.nit(), datos.razonSocial().trim(), vacioANulo(datos.nombreComercial()),
                datos.municipio().trim(), vacioANulo(datos.contacto())));
        actividad.registrar(usuario.getId(), "REGISTRO", "usuario", usuario.getId(), "Registro de empresa");
        return estadoPerfil.respuesta(usuario);
    }

    private Usuario crearUsuario(String correo, String contrasena, Rol rol, String nombre) {
        Usuario usuario = new Usuario(correo, codificador.encode(contrasena), rol, nombre);
        usuario.setFechaAceptacionPolitica(LocalDateTime.now());
        return usuarios.save(usuario);
    }

    private void validarCorreoLibre(String correo) {
        if (usuarios.existsByCorreo(correo)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
    }

    // ---------- Sesión (RF-02, RNF-02) ----------

    @Transactional(noRollbackFor = NegocioException.class)
    public SesionEmitida iniciarSesion(LoginRequest datos) {
        String correo = normalizarCorreo(datos.correo());
        if (intentosLogin.estaBloqueado(correo)) {
            throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos fallidos. Intenta de nuevo en " + intentosLogin.minutosBloqueo() + " minutos.");
        }
        Usuario usuario = usuarios.findByCorreo(correo).orElse(null);
        String hash = usuario != null ? usuario.getContrasenaHash() : hashFicticio;
        if (!codificador.matches(datos.contrasena(), hash) || usuario == null) {
            intentosLogin.registrarFallo(correo);
            throw new NegocioException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        intentosLogin.registrarExito(correo);
        if (!usuario.isActivo()) {
            throw new NegocioException(HttpStatus.FORBIDDEN,
                    "Tu cuenta está desactivada. Comunícate con el administrador de la plataforma.");
        }
        return emitirSesion(usuario);
    }

    /**
     * Rota el refresh token: el usado queda revocado y se entrega uno nuevo.
     * Si llega un token ya revocado, se asume robo y se cierran todas las sesiones del usuario.
     */
    @Transactional(noRollbackFor = NegocioException.class)
    public SesionEmitida renovar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, SESION_INVALIDA);
        }
        RefreshToken guardado = refreshTokens.findByTokenHash(Tokens.hash(refreshToken))
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, SESION_INVALIDA));
        Usuario usuario = guardado.getUsuario();
        if (guardado.isRevocado()) {
            refreshTokens.revocarTodos(usuario.getId());
            throw new NegocioException(HttpStatus.UNAUTHORIZED, SESION_INVALIDA);
        }
        if (!guardado.estaVigente() || !usuario.isActivo()) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, SESION_INVALIDA);
        }
        guardado.revocar();
        return emitirSesion(usuario);
    }

    public void cerrarSesion(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(Tokens.hash(refreshToken)).ifPresent(RefreshToken::revocar);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuarioActual(Long usuarioId) {
        return usuarios.findById(usuarioId).map(estadoPerfil::respuesta)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, SESION_INVALIDA));
    }

    private SesionEmitida emitirSesion(Usuario usuario) {
        String refresh = Tokens.generar();
        refreshTokens.save(new RefreshToken(usuario, Tokens.hash(refresh), LocalDateTime.now().plus(duracionRefresh)));
        SesionResponse sesion = new SesionResponse(jwtService.generarAcceso(usuario),
                jwtService.getDuracionAcceso().toSeconds(), estadoPerfil.respuesta(usuario));
        return new SesionEmitida(sesion, refresh, duracionRefresh);
    }

    // ---------- Recuperación de contraseña (RF-03) ----------

    /** Siempre responde igual, exista o no el correo, para no revelar qué cuentas están registradas. */
    public void solicitarRecuperacion(RecuperarRequest datos) {
        usuarios.findByCorreo(normalizarCorreo(datos.correo()))
                .filter(Usuario::isActivo)
                .ifPresent(usuario -> {
                    tokensRecuperacion.invalidarPendientes(usuario.getId());
                    String token = Tokens.generar();
                    tokensRecuperacion.save(new TokenRecuperacion(usuario, Tokens.hash(token),
                            LocalDateTime.now().plus(duracionRecuperacion)));
                    String enlace = frontendUrl + "/restablecer?token=" + token;
                    correoService.enviarRecuperacion(usuario.getCorreo(), enlace, duracionRecuperacion.toMinutes());
                });
    }

    public void restablecerContrasena(RestablecerRequest datos) {
        TokenRecuperacion token = tokensRecuperacion.findByTokenHash(Tokens.hash(datos.token()))
                .filter(TokenRecuperacion::estaVigente)
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        "El enlace de recuperación no es válido o ya venció. Solicita uno nuevo."));
        Usuario usuario = token.getUsuario();
        usuario.setContrasenaHash(codificador.encode(datos.nuevaContrasena()));
        token.marcarUsado();
        refreshTokens.revocarTodos(usuario.getId());
        intentosLogin.registrarExito(usuario.getCorreo());
        actividad.registrar(usuario.getId(), "RESTABLECER_CONTRASENA", "usuario", usuario.getId(),
                "Contraseña restablecida por enlace de recuperación");
    }

    // ---------- Utilidades ----------

    public static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
