package pe.clinicasanfelipe.citas;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ServiciosApiTest extends ApiTestBase {

    private Map<String, Object> servicio(String nombre) {
        return Map.of("nombre", nombre, "descripcion", "Descripcion", "duracionMinutos", 20, "precio", 45.50);
    }

    @Test
    void pacientePuedeListarPeroNoCrearServicios() throws Exception {
        String paciente = registrarPaciente("lector@correo.com");

        mockMvc.perform(get("/api/servicios").header("Authorization", bearer(paciente)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", bearer(paciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicio("Dermatologia"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarServiciosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/servicios")).andExpect(status().isUnauthorized());
    }

    @Test
    void administradorHaceCrudDeServicios() throws Exception {
        String admin = tokenAdmin();

        String creado = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicio("Dermatologia"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(creado).get("id").asLong();

        mockMvc.perform(put("/api/servicios/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Dermatologia Clinica", "descripcion", "Nueva",
                                "duracionMinutos", 40, "precio", 90.00, "activo", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dermatologia Clinica"))
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(get("/api/servicios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracionMinutos").value(40));

        mockMvc.perform(delete("/api/servicios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/servicios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaServicioInvalidoYNombreDuplicado() throws Exception {
        String admin = tokenAdmin();

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "", "duracionMinutos", -5, "precio", 10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.duracionMinutos").exists());

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicio("medicina general"))))
                .andExpect(status().isConflict());
    }

    @Test
    void consultaJpqlBuscaPorNombreIgnorandoMayusculas() throws Exception {
        String admin = tokenAdmin();
        mockMvc.perform(get("/api/servicios").param("nombre", "CARDIO").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Cardiologia"));
    }
}
