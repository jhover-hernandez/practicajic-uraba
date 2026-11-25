package co.edu.elpoli.practicajic;

import co.edu.elpoli.practicajic.auth.CorreoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tarea 15 · Recorre los criterios de aceptación del piloto (SRS, sección 8) de punta a punta:
 * empresa validada → oferta aprobada por el comité → estudiante postulado → seleccionado →
 * práctica formalizada con seguimientos y evaluación final → indicadores del panel.
 * Además comprueba las reglas de negocio RN-01, RN-02, RN-05 a RN-08 y RNF-04 y RNF-07.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PilotoCompletoTest {

    private static final String CLAVE = "Clave12345";
    private static final byte[] PDF = "%PDF-1.4\n% hoja de vida de prueba\n%%EOF".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockBean
    private CorreoService correoService;

    @Test
    void recorridoCompletoDelPiloto() throws Exception {
        String admin = tokenAdmin();

        // 1. La empresa se registra; sin validación no puede publicar (RN-01).
        String empresa = registrarEmpresa("piloto@empresa.co", "900555001-1");
        enviar(post("/api/empresa/ofertas"), empresa, oferta("Desarrollador junior", true)).andExpect(status().isForbidden());
        validarEmpresa(admin, "900555001-1");

        // 2. Publica la oferta y el administrador le asigna evaluador (RF-08, RF-09, RF-34).
        long ofertaId = id(enviar(post("/api/empresa/ofertas"), empresa, oferta("Desarrollador junior", true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_REVISION")));
        long comiteId = crearComite(admin, "comite.piloto@poli.edu.co");
        enviar(put("/api/admin/ofertas/" + ofertaId + "/evaluador"), admin, Map.of("evaluadorId", comiteId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.evaluadorId").value(comiteId));

        // 3. El comité la califica con la matriz de 100 puntos (RF-29, RF-30, RN-02).
        String comite = login("comite.piloto@poli.edu.co");
        assertThat(cuerpo(con(get("/api/comite/ofertas"), comite)).findValuesAsText("titulo")).contains("Desarrollador junior");
        enviar(post("/api/comite/ofertas/" + ofertaId + "/evaluacion"), comite, matrizOferta(10, "APROBAR"))
                .andExpect(status().isBadRequest());
        enviar(post("/api/comite/ofertas/" + ofertaId + "/evaluacion"), comite, matrizOferta(22, "APROBAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oferta.estado").value("APROBADA"))
                .andExpect(jsonPath("$.oferta.abierta").value(true));

        // 4. El estudiante se registra; necesita validación y hoja de vida para postularse (RN-05, RNF-04).
        String estudiante = registrarEstudiante("piloto@estudiante.co", "55001");
        con(post("/api/estudiante/convocatorias/" + ofertaId + "/postular"), estudiante).andExpect(status().isForbidden());
        validarEstudiante(admin, "103855001");
        con(post("/api/estudiante/convocatorias/" + ofertaId + "/postular"), estudiante).andExpect(status().isForbidden());
        subirHojaVida(estudiante, "foto.pdf", "no soy un pdf".getBytes()).andExpect(status().isBadRequest());
        subirHojaVida(estudiante, "Hoja de vida.pdf", PDF).andExpect(status().isOk())
                .andExpect(jsonPath("$.hojaVida.version").value(1))
                .andExpect(jsonPath("$.puedePostularse").value(true));
        enviar(put("/api/estudiante/competencias"), estudiante,
                Map.of("competencias", List.of(Map.of("competenciaId", competencia("Java"), "nivel", "INTERMEDIO")))).andExpect(status().isOk());

        // 5. Ve la convocatoria con sus competencias coincidentes y se postula una sola vez (RF-17, RF-25, RN-06).
        JsonNode convocatorias = cuerpo(con(get("/api/estudiante/convocatorias?q=junior"), estudiante));
        JsonNode convocatoria = buscar(convocatorias, "/oferta/id", ofertaId);
        assertThat(convocatoria.get("coincidentes").asInt()).isEqualTo(1);
        long postulacionId = id(con(post("/api/estudiante/convocatorias/" + ofertaId + "/postular"), estudiante)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("POSTULADO")));
        con(post("/api/estudiante/convocatorias/" + ofertaId + "/postular"), estudiante).andExpect(status().isConflict());

        // 6. Solo la empresa de la oferta ve la hoja de vida (RNF-07).
        String otraEmpresa = registrarEmpresa("otra@empresa.co", "900555002-2");
        validarEmpresa(admin, "900555002-2");
        con(get("/api/empresa/postulaciones/" + postulacionId + "/hoja-vida"), otraEmpresa).andExpect(status().isNotFound());
        con(get("/api/empresa/postulaciones/" + postulacionId + "/hoja-vida"), empresa)
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // 7. La empresa ve al postulante y lo lleva por el flujo hasta "Seleccionado" (RF-10, RF-24, RF-26).
        JsonNode postulantes = cuerpo(con(get("/api/empresa/ofertas/" + ofertaId + "/postulantes"), empresa));
        assertThat(postulantes.get(0).get("coincidentes").asInt()).isEqualTo(1);
        cambiarEstado(empresa, postulacionId, Map.of("estado", "EN_REVISION")).andExpect(status().isOk());
        cambiarEstado(empresa, postulacionId, Map.of("estado", "ENTREVISTA",
                "fechaEntrevista", LocalDateTime.now().plusDays(3).withNano(0).toString())).andExpect(status().isConflict());
        cambiarEstado(empresa, postulacionId, Map.of("estado", "PRESELECCIONADO")).andExpect(status().isOk());
        cambiarEstado(empresa, postulacionId, Map.of("estado", "ENTREVISTA")).andExpect(status().isBadRequest());
        cambiarEstado(empresa, postulacionId, Map.of("estado", "ENTREVISTA", "lugarEntrevista", "Sede Apartadó",
                "fechaEntrevista", LocalDateTime.now().plusDays(3).withNano(0).toString())).andExpect(status().isOk());
        cambiarEstado(empresa, postulacionId, Map.of("estado", "EN_REVISION")).andExpect(status().isConflict());

        // 8. El comité califica al candidato con la matriz del estudiante y lo recomienda (RF-31, RF-32).
        enviar(put("/api/comite/candidatos/" + postulacionId + "/evaluacion"), comite, matrizCandidato())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miEvaluacion.puntajeFinal").value(85.0))
                .andExpect(jsonPath("$.postulante.comite.recomendado").value(true));
        cambiarEstado(empresa, postulacionId, Map.of("estado", "SELECCIONADO")).andExpect(status().isOk());

        // 9. Una segunda postulación abierta del mismo estudiante...
        long segundaOferta = ofertaAprobada(empresa, comite, "Soporte de sistemas");
        long segundaPostulacion = id(con(post("/api/estudiante/convocatorias/" + segundaOferta + "/postular"), estudiante)
                .andExpect(status().isCreated()));

        // ...pasa a "No seleccionado" cuando acepta la primera práctica (RF-27, RN-08).
        con(post("/api/estudiante/postulaciones/" + postulacionId + "/aceptar"), estudiante)
                .andExpect(status().isOk()).andExpect(jsonPath("$.fechaAceptacion").isNotEmpty());
        JsonNode mias = cuerpo(con(get("/api/estudiante/postulaciones"), estudiante));
        assertThat(buscar(mias, "/id", segundaPostulacion).get("estado").asText()).isEqualTo("NO_SELECCIONADO");
        con(post("/api/estudiante/convocatorias/" + segundaOferta + "/postular"), estudiante).andExpect(status().isConflict());

        // 10. El administrador formaliza la práctica; se registran dos seguimientos y la evaluación final (RF-35 a RF-37).
        assertThat(cuerpo(con(get("/api/admin/practicas/pendientes"), admin)).findValuesAsText("postulacionId"))
                .contains(String.valueOf(postulacionId));
        long practicaId = id(enviar(post("/api/admin/practicas/formalizar/" + postulacionId), admin, Map.of(
                "fechaInicio", LocalDate.now().toString(), "tipoVinculacion", "Contrato de aprendizaje",
                "incentivo", 1300000, "tutorEmpresa", "Laura Gómez", "tutorInstitucion", "Docente APIT"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("INICIADA")));
        seguimiento(admin, "/api/admin/practicas/" + practicaId, "VISITA", null).andExpect(status().isCreated());
        seguimiento(empresa, "/api/empresa/practicas/" + practicaId, "EVALUACION_PARCIAL", null).andExpect(status().isBadRequest());
        seguimiento(empresa, "/api/empresa/practicas/" + practicaId, "EVALUACION_PARCIAL", 4.5).andExpect(status().isCreated());
        seguimiento(otraEmpresa, "/api/empresa/practicas/" + practicaId, "VISITA", null).andExpect(status().isNotFound());
        seguimiento(empresa, "/api/empresa/practicas/" + practicaId, "EVALUACION_FINAL", 4.8)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"))
                .andExpect(jsonPath("$.calificacionFinal").value(4.8))
                .andExpect(jsonPath("$.seguimientos.length()").value(3));
        assertThat(cuerpo(con(get("/api/estudiante/practicas"), estudiante)).get(0).get("estado").asText())
                .isEqualTo("FINALIZADA");

        // 11. El panel muestra los indicadores con los datos de esta prueba (RF-38 a RF-40).
        JsonNode panel = cuerpo(con(get("/api/admin/panel"), admin));
        assertThat(panel.get("empresasValidadas").asLong()).isGreaterThanOrEqualTo(2);
        assertThat(panel.get("practicas").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(panel.get("tasaColocacion").decimalValue()).isPositive();
        String csv = con(get("/api/admin/informes/practicas"), admin).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("103855001").contains("Laura Gómez");

        // 12. El acta del día recoge las evaluaciones del comité (RF-33).
        JsonNode acta = cuerpo(con(get("/api/comite/acta?desde=" + LocalDate.now() + "&hasta=" + LocalDate.now()), comite));
        assertThat(acta.get("ofertas").size()).isGreaterThanOrEqualTo(2);
        assertThat(acta.get("candidatos").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void lasRutasNuevasRespetanElRolDeCadaUsuario() throws Exception {
        String estudiante = registrarEstudiante("rol.nuevo@estudiante.co", "55009");
        con(get("/api/admin/panel"), estudiante).andExpect(status().isForbidden());
        con(get("/api/admin/validaciones/empresas"), estudiante).andExpect(status().isForbidden());
        con(get("/api/comite/candidatos"), estudiante).andExpect(status().isForbidden());
        con(get("/api/empresa/practicas"), estudiante).andExpect(status().isForbidden());
        con(get("/api/catalogos/areas"), estudiante).andExpect(status().isOk());
        enviar(post("/api/admin/catalogos/areas"), estudiante, Map.of("nombre", "Intrusa")).andExpect(status().isForbidden());
        mvc.perform(get("/api/catalogos/areas")).andExpect(status().isUnauthorized());
    }

    @Test
    void elAdministradorGestionaElCatalogo() throws Exception {
        String admin = tokenAdmin();
        long areaId = id(enviar(post("/api/admin/catalogos/areas"), admin, Map.of("nombre", "Ciberseguridad"))
                .andExpect(status().isCreated()));
        enviar(post("/api/admin/catalogos/areas"), admin, Map.of("nombre", "ciberseguridad")).andExpect(status().isConflict());
        enviar(put("/api/admin/catalogos/areas/" + areaId), admin, Map.of("nombre", "Seguridad informática"))
                .andExpect(status().isOk());
        con(delete("/api/admin/catalogos/areas/" + areaId), admin).andExpect(status().isNoContent());
        enviar(post("/api/admin/catalogos/competencias"), admin, Map.of("nombre", "Python", "categoria", "TECNICA"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoria").value("TECNICA"));
    }

    @Test
    void rechazarSinMotivoNoSePermiteYCorregirLosDatosVuelveARevision() throws Exception {
        String admin = tokenAdmin();
        String empresa = registrarEmpresa("rechazada@empresa.co", "900555003-3");
        long id = idEmpresa(admin, "900555003-3");
        enviar(patch("/api/admin/validaciones/empresas/" + id), admin, Map.of("decision", "RECHAZAR"))
                .andExpect(status().isBadRequest());
        enviar(patch("/api/admin/validaciones/empresas/" + id), admin, Map.of("decision", "RECHAZAR", "motivo", "NIT no coincide"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estadoValidacion").value("RECHAZADA"));
        enviar(put("/api/empresa/perfil"), empresa, Map.of("razonSocial", "Empresa Corregida S.A.S.", "municipio", "Turbo",
                "sector", "Tecnología", "tamano", "PEQUENA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoValidacion").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivoRechazo").doesNotExist());
    }

    // ---------- Utilidades ----------

    private long ofertaAprobada(String empresa, String comite, String titulo) throws Exception {
        long id = id(enviar(post("/api/empresa/ofertas"), empresa, oferta(titulo, true)).andExpect(status().isCreated()));
        enviar(post("/api/comite/ofertas/" + id + "/evaluacion"), comite, matrizOferta(20, "APROBAR")).andExpect(status().isOk());
        return id;
    }

    private Map<String, Object> oferta(String titulo, boolean enviarARevision) throws Exception {
        Map<String, Object> o = new HashMap<>();
        o.put("areaId", cuerpo(mvc.perform(conToken(get("/api/catalogos/areas"), tokenAdmin()))).get(0).get("id").asLong());
        o.put("titulo", titulo);
        o.put("descripcion", "Apoyo al equipo de tecnología de la empresa.");
        o.put("actividades", "Desarrollo y mantenimiento de aplicaciones.");
        o.put("competencias", List.of(Map.of("competenciaId", competencia("Java"), "nivelRequerido", "BASICO"),
                Map.of("competenciaId", competencia("SQL"), "nivelRequerido", "BASICO")));
        o.put("modalidad", "PRESENCIAL");
        o.put("municipio", "Apartadó");
        o.put("duracionMeses", 6);
        o.put("vacantes", 1);
        o.put("incentivo", 1300000);
        o.put("tipoVinculacion", "Contrato de aprendizaje");
        o.put("fechaCierre", LocalDate.now().plusDays(30).toString());
        o.put("enviarARevision", enviarARevision);
        return o;
    }

    /** Puntajes proporcionales sobre 25 para los seis criterios de la matriz de empresas. */
    private static Map<String, Object> matrizOferta(int base, String decision) {
        double f = base / 25.0;
        java.util.function.DoubleUnaryOperator r = x -> Math.round(x * f * 100) / 100.0;
        return Map.of("pertinencia", r.applyAsDouble(25), "calidadPlan", r.applyAsDouble(20), "condiciones", r.applyAsDouble(20),
                "incentivoEconomico", r.applyAsDouble(15), "acompanamiento", r.applyAsDouble(10), "experienciaLaboral", r.applyAsDouble(10),
                "decision", decision, "observaciones", "Evaluación de prueba");
    }

    private static Map<String, Object> matrizCandidato() {
        return Map.of("tecnico", 26, "perfil", 21, "academico", 13, "portafolio", 12, "blandas", 9, "entrevista", 4,
                "concepto", "Buen perfil técnico", "recomendado", true);
    }

    private ResultActions seguimiento(String token, String base, String tipo, Double nota) throws Exception {
        Map<String, Object> s = new HashMap<>(Map.of("tipo", tipo, "fecha", LocalDate.now().toString(),
                "descripcion", "Seguimiento de prueba"));
        if (nota != null) {
            s.put("calificacion", nota);
        }
        return enviar(post(base + "/seguimientos"), token, s);
    }

    private ResultActions cambiarEstado(String token, long postulacionId, Map<String, Object> datos) throws Exception {
        return enviar(patch("/api/empresa/postulaciones/" + postulacionId + "/estado"), token, datos);
    }

    private ResultActions subirHojaVida(String token, String nombre, byte[] contenido) throws Exception {
        return mvc.perform(conToken(multipart("/api/estudiante/hoja-vida")
                .file(new MockMultipartFile("archivo", nombre, "application/pdf", contenido)), token));
    }

    private String registrarEmpresa(String correo, String nit) throws Exception {
        mvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("correo", correo, "contrasena", CLAVE, "nit", nit,
                        "razonSocial", "Empresa " + nit, "municipio", "Apartadó", "aceptaPoliticaDatos", true))))
                .andExpect(status().isCreated());
        return login(correo);
    }

    private String registrarEstudiante(String correo, String documento) throws Exception {
        mvc.perform(post("/api/auth/registro/estudiante").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("correo", correo, "contrasena", CLAVE, "documento", "1038" + documento,
                        "nombres", "Ana", "apellidos", "Pérez", "programa", "Tecnología en Desarrollo de Software",
                        "semestre", 6, "municipio", "Apartadó", "aceptaPoliticaDatos", true))))
                .andExpect(status().isCreated());
        return login(correo);
    }

    private long crearComite(String admin, String correo) throws Exception {
        return id(enviar(post("/api/admin/usuarios"), admin,
                Map.of("correo", correo, "nombre", "Miembro del comité", "rol", "COMITE", "contrasena", CLAVE))
                .andExpect(status().isCreated()));
    }

    private void validarEmpresa(String admin, String nit) throws Exception {
        enviar(patch("/api/admin/validaciones/empresas/" + idEmpresa(admin, nit)), admin, Map.of("decision", "VALIDAR"))
                .andExpect(status().isOk());
    }

    private long idEmpresa(String admin, String nit) throws Exception {
        return buscar(cuerpo(con(get("/api/admin/validaciones/empresas"), admin)), "/nit", nit).get("id").asLong();
    }

    private void validarEstudiante(String admin, String documento) throws Exception {
        long id = buscar(cuerpo(con(get("/api/admin/validaciones/estudiantes?estado=PENDIENTE"), admin)), "/documento", documento)
                .get("id").asLong();
        enviar(patch("/api/admin/validaciones/estudiantes/" + id), admin, Map.of("decision", "VALIDAR"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("VALIDADO"));
    }

    private long competencia(String nombre) throws Exception {
        return buscar(cuerpo(mvc.perform(conToken(get("/api/catalogos/competencias"), tokenAdmin()))), "/nombre", nombre)
                .get("id").asLong();
    }

    private String tokenAdmin() throws Exception {
        return loginCon("admin@test.local", "Admin12345");
    }

    private String login(String correo) throws Exception {
        return loginCon(correo, CLAVE);
    }

    private String loginCon(String correo, String clave) throws Exception {
        String r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("correo", correo, "contrasena", clave))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).get("accessToken").asText();
    }

    private ResultActions con(MockHttpServletRequestBuilder peticion, String token) throws Exception {
        return mvc.perform(conToken(peticion, token));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, Object cuerpo) throws Exception {
        return mvc.perform(conToken(peticion, token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(cuerpo)));
    }

    private static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String token) {
        return peticion.header("Authorization", "Bearer " + token);
    }

    private JsonNode cuerpo(ResultActions r) throws Exception {
        return json.readTree(r.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long id(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    /** Busca en un arreglo JSON el elemento cuyo campo (puntero JSON) tiene el valor dado. */
    private static JsonNode buscar(JsonNode arreglo, String puntero, Object valor) {
        for (JsonNode n : arreglo) {
            if (n.at(puntero).asText().equals(String.valueOf(valor))) {
                return n;
            }
        }
        throw new AssertionError("No se encontró " + puntero + " = " + valor + " en " + arreglo);
    }
}
