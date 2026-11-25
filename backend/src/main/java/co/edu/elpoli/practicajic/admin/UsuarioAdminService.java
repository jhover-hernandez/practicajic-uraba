package co.edu.elpoli.practicajic.admin;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.admin.dto.CambiarEstadoRequest;
import co.edu.elpoli.practicajic.admin.dto.CrearUsuarioRequest;
import co.edu.elpoli.practicajic.admin.dto.EditarUsuarioRequest;
import co.edu.elpoli.practicajic.admin.dto.UsuarioAdminResponse;
import co.edu.elpoli.practicajic.auth.AuthService;
import co.edu.elpoli.practicajic.auth.RefreshTokenRepository;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.usuario.EstadoPerfil;
import co.edu.elpoli.practicajic.usuario.Rol;
import co.edu.elpoli.practicajic.usuario.Usuario;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/** RF-05: el administrador crea, edita, activa y desactiva usuarios. Cada cambio queda registrado (RNF-05). */
@Service
@Transactional
public class UsuarioAdminService {

    /** Roles que el administrador puede crear o asignar. Estudiantes y empresas tienen perfil propio. */
    private static final Set<Rol> ROLES_INTERNOS = EnumSet.of(Rol.ADMIN, Rol.COMITE);

    private final UsuarioRepository usuarios;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder codificador;
    private final ActividadService actividad;
    private final EstadoPerfil estadoPerfil;

    public UsuarioAdminService(UsuarioRepository usuarios, RefreshTokenRepository refreshTokens,
                               PasswordEncoder codificador, ActividadService actividad, EstadoPerfil estadoPerfil) {
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.codificador = codificador;
        this.actividad = actividad;
        this.estadoPerfil = estadoPerfil;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioAdminResponse> listar(Rol rol, Boolean activo, String busqueda, Pageable pagina) {
        Specification<Usuario> filtro = Specification.where(null);
        if (rol != null) {
            filtro = filtro.and((u, q, cb) -> cb.equal(u.get("rol"), rol));
        }
        if (activo != null) {
            filtro = filtro.and((u, q, cb) -> cb.equal(u.get("activo"), activo));
        }
        if (busqueda != null && !busqueda.isBlank()) {
            String patron = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
            filtro = filtro.and((u, q, cb) -> cb.or(
                    cb.like(cb.lower(u.<String>get("correo")), patron),
                    cb.like(cb.lower(cb.coalesce(u.<String>get("nombre"), "")), patron)));
        }
        return usuarios.findAll(filtro, pagina).map(this::respuesta);
    }

    public UsuarioAdminResponse crear(CrearUsuarioRequest datos, Long actorId) {
        validarRolInterno(datos.rol());
        String correo = AuthService.normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = usuarios.save(
                new Usuario(correo, codificador.encode(datos.contrasena()), datos.rol(), datos.nombre().trim()));
        actividad.registrar(actorId, "CREAR_USUARIO", "usuario", usuario.getId(), "Rol " + datos.rol());
        return respuesta(usuario);
    }

    public UsuarioAdminResponse editar(Long id, EditarUsuarioRequest datos, Long actorId) {
        Usuario usuario = buscar(id);
        String correo = AuthService.normalizarCorreo(datos.correo());
        if (!correo.equals(usuario.getCorreo()) && usuarios.existsByCorreo(correo)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        if (datos.rol() != usuario.getRol()) {
            if (usuario.getId().equals(actorId)) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
            }
            if (!ROLES_INTERNOS.contains(usuario.getRol())) {
                throw new NegocioException(HttpStatus.BAD_REQUEST,
                        "El rol de estudiantes y empresas no se puede cambiar porque tienen un perfil asociado.");
            }
            validarRolInterno(datos.rol());
        }
        String detalle = "Antes: " + usuario.getCorreo() + " / " + usuario.getRol()
                + " · Ahora: " + correo + " / " + datos.rol();
        usuario.setCorreo(correo);
        usuario.setNombre(datos.nombre().trim());
        usuario.setRol(datos.rol());
        actividad.registrar(actorId, "EDITAR_USUARIO", "usuario", usuario.getId(), detalle);
        return respuesta(usuario);
    }

    public UsuarioAdminResponse cambiarEstado(Long id, CambiarEstadoRequest datos, Long actorId) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(actorId)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta.");
        }
        if (usuario.isActivo() != datos.activo()) {
            usuario.setActivo(datos.activo());
            if (!datos.activo()) {
                refreshTokens.revocarTodos(usuario.getId());
            }
            actividad.registrar(actorId, datos.activo() ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                    "usuario", usuario.getId(), null);
        }
        return respuesta(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "El usuario no existe."));
    }

    private static void validarRolInterno(Rol rol) {
        if (!ROLES_INTERNOS.contains(rol)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST,
                    "Desde aquí solo se crean administradores y miembros del comité. "
                            + "Estudiantes y empresas se registran desde el formulario público.");
        }
    }

    private UsuarioAdminResponse respuesta(Usuario u) {
        return new UsuarioAdminResponse(u.getId(), u.getCorreo(), u.getNombre(), u.getRol(), u.isActivo(),
                u.getFechaCreacion(), estadoPerfil.de(u));
    }
}
