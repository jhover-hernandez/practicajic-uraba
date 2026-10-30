package co.edu.elpoli.practicajic.seguridad;

import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el encabezado "Authorization: Bearer ..." y autentica la petición.
 * Consulta el usuario en cada petición para que una cuenta desactivada pierda el acceso de inmediato.
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtFiltro(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            jwtService.usuarioIdSiValido(encabezado.substring(PREFIJO.length()))
                    .flatMap(usuarios::findById)
                    .filter(usuario -> usuario.isActivo())
                    .ifPresent(usuario -> {
                        var principal = new UsuarioAutenticado(usuario.getId(), usuario.getCorreo(), usuario.getRol());
                        var permisos = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                        var autenticacion = new UsernamePasswordAuthenticationToken(principal, null, permisos);
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }
        cadena.doFilter(request, response);
    }
}
