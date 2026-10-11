package pe.clinicasanfelipe.citas;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CitasApiTest extends ApiTestBase {

    private static final LocalDateTime FECHA = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0)
            .withSecond(0).withNano(0);

    private Map<String, Object> cita(long servicioId, LocalDateTime fecha) {
        return Map.of("servicioId", servicioId, "fechaHora", fecha.toString(), "motivo", "Control");
    }

    private long crearCita(String token, long servicioId, LocalDateTime fecha) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/citas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cita(servicioId, fecha))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).get("id").asLong();
    }

    @Test
    void pacienteCreaYConsultaSuCita() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Traumatologia");
        String paciente = registrarPaciente("p1@correo.com");

        long id = crearCita(paciente, servicio, FECHA);

        mockMvc.perform(get("/api/citas/" + id).header("Authorization", bearer(paciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicioNombre").value("Traumatologia"));
        mockMvc.perform(get("/api/citas").header("Authorization", bearer(paciente)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void pacienteNoVeCitasDeOtrosPeroAdministradorVeTodas() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Oftalmologia");
        String paciente1 = registrarPaciente("p2@correo.com");
        String paciente2 = registrarPaciente("p3@correo.com");
        long id = crearCita(paciente1, servicio, FECHA);

        mockMvc.perform(get("/api/citas/" + id).header("Authorization", bearer(paciente2)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/citas").header("Authorization", bearer(paciente2)))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/citas").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void rechazaFechaPasadaYHorarioOcupado() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Neurologia");
        String paciente1 = registrarPaciente("p4@correo.com");
        String paciente2 = registrarPaciente("p5@correo.com");

        mockMvc.perform(post("/api/citas")
                        .header("Authorization", bearer(paciente1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cita(servicio, LocalDateTime.now().minusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fechaHora").exists());

        crearCita(paciente1, servicio, FECHA);
        mockMvc.perform(post("/api/citas")
                        .header("Authorization", bearer(paciente2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cita(servicio, FECHA))))
                .andExpect(status().isConflict());
    }

    @Test
    void reprogramaYCancelaLaCita() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Urologia");
        String paciente = registrarPaciente("p6@correo.com");
        long id = crearCita(paciente, servicio, FECHA);

        mockMvc.perform(put("/api/citas/" + id)
                        .header("Authorization", bearer(paciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cita(servicio, FECHA.plusHours(2)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaHora").value(
                        FECHA.plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        mockMvc.perform(delete("/api/citas/" + id).header("Authorization", bearer(paciente)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/citas/" + id).header("Authorization", bearer(paciente)))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
        mockMvc.perform(delete("/api/citas/" + id).header("Authorization", bearer(paciente)))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeEliminarServicioNiUsuarioConCitas() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Gastroenterologia");
        String paciente = registrarPaciente("p7@correo.com");
        crearCita(paciente, servicio, FECHA);
        String perfil = mockMvc.perform(get("/api/usuarios/me").header("Authorization", bearer(paciente)))
                .andReturn().getResponse().getContentAsString();
        long idPaciente = objectMapper.readTree(perfil).get("id").asLong();

        mockMvc.perform(delete("/api/servicios/" + servicio).header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/usuarios/" + idPaciente).header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void noSeAgendaEnServicioInactivo() throws Exception {
        String admin = tokenAdmin();
        long servicio = crearServicio(admin, "Endocrinologia");
        mockMvc.perform(put("/api/servicios/" + servicio)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Endocrinologia", "duracionMinutos", 30,
                                "precio", 70, "activo", false))))
                .andExpect(status().isOk());
        String paciente = registrarPaciente("p8@correo.com");

        mockMvc.perform(post("/api/citas")
                        .header("Authorization", bearer(paciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cita(servicio, FECHA))))
                .andExpect(status().isBadRequest());
    }
}
