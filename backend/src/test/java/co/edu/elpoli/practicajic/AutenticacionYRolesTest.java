package co.edu.elpoli.practicajic;

import co.edu.elpoli.practicajic.actividad.RegistroActividadRepository;
import co.edu.elpoli.practicajic.auth.CorreoService;
import co.edu.elpoli.practicajic.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la Tarea 9 contra los criterios de aceptación del SRS (sección 8):
 * "un usuario no puede entrar a endpoints de otro rol" y "ninguna contraseña aparece en texto plano".
 * Cada prueba usa correos distintos porque comparten la misma base en memoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutenticacionYRolesTest {

    private static final String CLAVE = "Clave12345";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private RegistroActividadRepository actividades;
    @MockBean
    private CorreoService correoService;

    // ---------- RF-01 · Registro ----------

    @Test
    void registraEstudianteEnEstadoPendienteYGuardaLaContrasenaConBcrypt() throws Exception {
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(estudiante("Ana@Correo.co", "1001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.estadoPerfil").value("PENDIENTE"))
                .andExpect(jsonPath("$.correo").value("ana@correo.co"));

        var usuario = usuarios.findByCorreo("ana@correo.co").orElseThrow();
        assertThat(usuario.getContrasenaHash()).startsWith("$2").doesNotContain(CLAVE);
        assertThat(usuario.getFechaAceptacionPolitica()).isNotNull();
    }

    @Test
    void registraEmpresaEnEstadoPendiente() throws Exception {
        mvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(empresa("rrhh@empresa.co", "900123456-7"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("EMPRESA"))
                .andExpect(jsonPath("$.estadoPerfil").value("PENDIENTE"));
    }

    @Test
    void rechazaRegistroSinAceptarPoliticaDeDatos() throws Exception {
        Map<String, Object> datos = new java.util.HashMap<>(estudiante("sinpolitica@correo.co", "1002"));
        datos.put("aceptaPoliticaDatos", false);
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(datos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aceptaPoliticaDatos").exists());
    }

    @Test
    void rechazaCorreoDuplicadoYContrasenaDebil() throws Exception {
        registrarEstudiante("duplicado@correo.co", "1003");
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(estudiante("DUPLICADO@correo.co", "1004"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con ese correo."));

        Map<String, Object> debil = new java.util.HashMap<>(estudiante("debil@correo.co", "1005"));
        debil.put("contrasena", "12345678");
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(debil)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.contrasena").exists());
    }

    // ---------- RF-02 · Inicio y cierre de sesión ----------

    @Test
    void iniciaSesionYEntregaTokenYCookieHttpOnly() throws Exception {
        registrarEstudiante("login@correo.co", "1010");
        MvcResult r = login("login@correo.co", CLAVE).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("ESTUDIANTE"))
                .andReturn();
        Cookie cookie = r.getResponse().getCookie("pj_refresh");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();

        mvc.perform(conToken(get("/api/auth/yo"), token(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("login@correo.co"));
    }

    @Test
    void rechazaCredencialesInvalidasConMensajeGenerico() throws Exception {
        registrarEstudiante("malaclave@correo.co", "1011");
        login("malaclave@correo.co", "otra12345").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
        login("noexiste@correo.co", CLAVE).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    void bloqueaTemporalmenteTrasCincoIntentosFallidos() throws Exception {
        registrarEstudiante("fuerzabruta@correo.co", "1012");
        for (int i = 0; i < 5; i++) {
            login("fuerzabruta@correo.co", "mala12345").andExpect(status().isUnauthorized());
        }
        login("fuerzabruta@correo.co", CLAVE).andExpect(status().isTooManyRequests());
    }

    @Test
    void renuevaElTokenRotandoLaCookieYDetectaReutilizacion() throws Exception {
        registrarEstudiante("refresh@correo.co", "1013");
        Cookie original = login("refresh@correo.co", CLAVE).andReturn().getResponse().getCookie("pj_refresh");

        MvcResult renovado = mvc.perform(post("/api/auth/refresh").cookie(original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        Cookie nueva = renovado.getResponse().getCookie("pj_refresh");
        assertThat(nueva.getValue()).isNotEqualTo(original.getValue());

        // Reusar el token viejo indica robo: se rechaza y se revocan todas las sesiones, incluida la nueva.
        mvc.perform(post("/api/auth/refresh").cookie(original)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").cookie(nueva)).andExpect(status().isUnauthorized());
    }

    @Test
    void cerrarSesionInvalidaElTokenDeRenovacion() throws Exception {
        registrarEstudiante("logout@correo.co", "1014");
        Cookie cookie = login("logout@correo.co", CLAVE).andReturn().getResponse().getCookie("pj_refresh");
        mvc.perform(post("/api/auth/logout").cookie(cookie)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
    }

    // ---------- RF-03 · Recuperar contraseña ----------

    @Test
    void recuperaLaContrasenaConEnlaceDeUnSoloUso() throws Exception {
        registrarEstudiante("olvido@correo.co", "1020");
        mvc.perform(post("/api/auth/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"olvido@correo.co\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarRecuperacion(eq("olvido@correo.co"), enlace.capture(), anyLong());
        String token = enlace.getValue().substring(enlace.getValue().indexOf("token=") + 6);

        String cuerpo = "{\"token\":\"" + token + "\",\"nuevaContrasena\":\"Nueva12345\"}";
        mvc.perform(post("/api/auth/restablecer").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
        login("olvido@correo.co", CLAVE).andExpect(status().isUnauthorized());
        login("olvido@correo.co", "Nueva12345").andExpect(status().isOk());

        mvc.perform(post("/api/auth/restablecer").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recuperarRespondeIgualAunqueElCorreoNoExista() throws Exception {
        mvc.perform(post("/api/auth/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"fantasma@correo.co\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ---------- RF-04 · RNF-03 · Acceso por rol en cada endpoint ----------

    @Test
    void sinTokenNoSePuedeEntrarARutasProtegidas() throws Exception {
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión para continuar."));
        mvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(conToken(get("/api/auth/yo"), "token.falso.invalido")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeEstudianteNoEntraARutasDeComiteNiAdminNiEmpresa() throws Exception {
        registrarEstudiante("intruso@correo.co", "1030");
        String token = token(login("intruso@correo.co", CLAVE).andReturn());

        mvc.perform(conToken(get("/api/comite/ofertas"), token)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Acceso denegado: tu rol no tiene permiso para esta acción."));
        mvc.perform(conToken(get("/api/admin/usuarios"), token)).andExpect(status().isForbidden());
        mvc.perform(conToken(post("/api/empresa/ofertas"), token)).andExpect(status().isForbidden());
    }

    @Test
    void tokenDeEmpresaNoEntraARutasDeEstudianteNiComite() throws Exception {
        mvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(empresa("empresa.rol@correo.co", "800111222-3"))));
        String token = token(login("empresa.rol@correo.co", CLAVE).andReturn());

        mvc.perform(conToken(get("/api/estudiante/postulaciones"), token)).andExpect(status().isForbidden());
        mvc.perform(conToken(get("/api/comite/ofertas"), token)).andExpect(status().isForbidden());
    }

    // ---------- RF-05 · Gestión de usuarios por el administrador ----------

    @Test
    void administradorCreaMiembroDelComiteQuePuedeIniciarSesion() throws Exception {
        String admin = tokenAdmin();
        mvc.perform(conToken(post("/api/admin/usuarios"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("correo", "comite1@poli.edu.co",
                                "nombre", "Docente del comité", "rol", "COMITE", "contrasena", CLAVE))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("COMITE"));

        MvcResult sesion = login("comite1@poli.edu.co", CLAVE).andExpect(status().isOk()).andReturn();
        // El comité no administra usuarios.
        mvc.perform(conToken(get("/api/admin/usuarios"), token(sesion))).andExpect(status().isForbidden());
    }

    @Test
    void administradorNoCreaEstudiantesNiEmpresasDesdeElPanel() throws Exception {
        mvc.perform(conToken(post("/api/admin/usuarios"), tokenAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("correo", "x@poli.edu.co",
                                "nombre", "X", "rol", "ESTUDIANTE", "contrasena", CLAVE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void desactivarUsuarioLeQuitaElAccesoDeInmediatoYQuedaRegistrado() throws Exception {
        registrarEstudiante("desactivar@correo.co", "1040");
        MvcResult sesion = login("desactivar@correo.co", CLAVE).andReturn();
        String tokenEstudiante = token(sesion);
        Cookie cookie = sesion.getResponse().getCookie("pj_refresh");
        Long id = usuarios.findByCorreo("desactivar@correo.co").orElseThrow().getId();

        mvc.perform(conToken(patch("/api/admin/usuarios/" + id + "/estado"), tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(conToken(get("/api/auth/yo"), tokenEstudiante)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
        login("desactivar@correo.co", CLAVE).andExpect(status().isForbidden());

        assertThat(actividades.findByEntidadAndEntidadIdOrderByFechaDesc("usuario", id))
                .extracting("accion").contains("DESACTIVAR_USUARIO");

        mvc.perform(conToken(patch("/api/admin/usuarios/" + id + "/estado"), tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":true}"))
                .andExpect(status().isOk());
        login("desactivar@correo.co", CLAVE).andExpect(status().isOk());
    }

    @Test
    void administradorNoPuedeDesactivarseASiMismo() throws Exception {
        Long id = usuarios.findByCorreo("admin@test.local").orElseThrow().getId();
        mvc.perform(conToken(patch("/api/admin/usuarios/" + id + "/estado"), tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administradorListaYFiltraUsuarios() throws Exception {
        registrarEstudiante("filtro.lista@correo.co", "1050");
        mvc.perform(conToken(get("/api/admin/usuarios?rol=ESTUDIANTE&q=filtro.lista"), tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].estadoPerfil").value("PENDIENTE"));
    }

    // ---------- Utilidades ----------

    private Map<String, Object> estudiante(String correo, String documento) {
        return Map.of("correo", correo, "contrasena", CLAVE, "documento", "1038" + documento,
                "nombres", "Ana", "apellidos", "Pérez", "programa", "Tecnología en Desarrollo de Software",
                "semestre", 6, "municipio", "Apartadó", "aceptaPoliticaDatos", true);
    }

    private Map<String, Object> empresa(String correo, String nit) {
        return Map.of("correo", correo, "contrasena", CLAVE, "nit", nit, "razonSocial", "Empresa Ejemplo S.A.S.",
                "municipio", "Turbo", "aceptaPoliticaDatos", true);
    }

    private void registrarEstudiante(String correo, String documento) throws Exception {
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(estudiante(correo, documento))))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions login(String correo, String contrasena)
            throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("correo", correo, "contrasena", contrasena))));
    }

    private String tokenAdmin() throws Exception {
        return token(login("admin@test.local", "Admin12345").andExpect(status().isOk()).andReturn());
    }

    private String token(MvcResult resultado) throws Exception {
        JsonNode cuerpo = json.readTree(resultado.getResponse().getContentAsString());
        return cuerpo.get("accessToken").asText();
    }

    private static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String token) {
        return peticion.header("Authorization", "Bearer " + token);
    }
}
