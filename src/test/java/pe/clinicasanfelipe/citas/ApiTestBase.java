package pe.clinicasanfelipe.citas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de las pruebas de integracion: levanta la aplicacion completa con H2 en memoria
 * (perfil "test") y revierte cada prueba al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestBase {

    protected static final String ADMIN_CORREO = "admin@clinicasanfelipe.pe";
    protected static final String ADMIN_PASSWORD = "admin12345";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String tokenAdmin() throws Exception {
        return login(ADMIN_CORREO, ADMIN_PASSWORD);
    }

    protected String login(String correo, String password) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("correo", correo, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = objectMapper.readTree(cuerpo);
        return nodo.get("token").asText();
    }

    /** Registra un paciente nuevo y devuelve su token JWT. */
    protected String registrarPaciente(String correo) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of(
                                "nombre", "Paciente de prueba",
                                "correo", correo,
                                "password", "paciente123",
                                "telefono", "987654321"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).get("token").asText();
    }

    /** Crea un servicio como administrador y devuelve su id. */
    protected long crearServicio(String adminToken, String nombre) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of(
                                "nombre", nombre,
                                "descripcion", "Servicio de prueba",
                                "duracionMinutos", 30,
                                "precio", 50.00))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).get("id").asLong();
    }
}
