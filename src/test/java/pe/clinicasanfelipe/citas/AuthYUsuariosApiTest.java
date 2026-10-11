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

class AuthYUsuariosApiTest extends ApiTestBase {

    @Test
    void registroCreaPacienteYDevuelveToken() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Ana", "correo", "ana@correo.com",
                                "password", "paciente123", "telefono", "999888777"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registroRechazaContrasenaSinNumeros() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Ana", "correo", "ana2@correo.com",
                                "password", "sololetras"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void registroRechazaCorreoDuplicado() throws Exception {
        registrarPaciente("dup@correo.com");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Otro", "correo", "dup@correo.com",
                                "password", "paciente123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("correo", ADMIN_CORREO, "password", "incorrecta1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void pacienteNoPuedeListarUsuarios() throws Exception {
        String token = registrarPaciente("paciente1@correo.com");
        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteVeSuPropioPerfil() throws Exception {
        String token = registrarPaciente("paciente2@correo.com");
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("paciente2@correo.com"));
    }

    @Test
    void administradorHaceCrudDeUsuarios() throws Exception {
        String admin = tokenAdmin();

        String creado = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Recepcion", "correo", "recepcion@clinicasanfelipe.pe",
                                "password", "recepcion1", "rol", "ADMINISTRADOR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(creado).get("id").asLong();

        mockMvc.perform(get("/api/usuarios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Recepcion"));

        mockMvc.perform(put("/api/usuarios/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Recepcion Central", "correo", "recepcion@clinicasanfelipe.pe",
                                "telefono", "911222333", "rol", "PACIENTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Recepcion Central"))
                .andExpect(jsonPath("$.rol").value("PACIENTE"));

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void administradorNoPuedeEliminarseASiMismo() throws Exception {
        String admin = tokenAdmin();
        String lista = mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        long idAdmin = objectMapper.readTree(lista).get(0).get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/" + idAdmin).header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void tokenDeUsuarioEliminadoYaNoAutentica() throws Exception {
        String admin = tokenAdmin();
        String tokenPaciente = registrarPaciente("temporal@correo.com");
        String perfil = mockMvc.perform(get("/api/usuarios/me").header("Authorization", bearer(tokenPaciente)))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(perfil).get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", bearer(tokenPaciente)))
                .andExpect(status().isUnauthorized());
    }
}
