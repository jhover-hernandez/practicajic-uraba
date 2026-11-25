package co.edu.elpoli.practicajic.config;

import co.edu.elpoli.practicajic.comun.ApiError;
import co.edu.elpoli.practicajic.seguridad.JwtFiltro;
import co.edu.elpoli.practicajic.seguridad.JwtService;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * RF-04 y RNF-03: el control de acceso por rol se aplica en la API, no solo en el frontend.
 * Cada módulo vive bajo el prefijo de su rol; lo que no esté listado exige al menos una sesión válida.
 */
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtService jwtService, UsuarioRepository usuarios,
                                        ObjectMapper json) throws Exception {
        http
                // API sin estado con JWT en encabezado; la cookie de renovación es SameSite=Strict.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, json, 401, "Debes iniciar sesión para continuar."))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, json, 403, "Acceso denegado: tu rol no tiene permiso para esta acción.")))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/registro/**", "/api/auth/refresh",
                                "/api/auth/logout", "/api/auth/recuperar", "/api/auth/restablecer").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/empresa/**").hasRole("EMPRESA")
                        .requestMatchers("/api/estudiante/**").hasRole("ESTUDIANTE")
                        .requestMatchers("/api/comite/**").hasRole("COMITE")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFiltro(jwtService, usuarios), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** RNF-01: BCrypt con costo 12. */
    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource configuracionCors(@Value("${app.cors-origenes}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }

    private static void escribirError(HttpServletResponse res, ObjectMapper json, int estado, String mensaje)
            throws IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), ApiError.de(estado, mensaje));
    }
}
