package pe.clinicasanfelipe.citas.cita;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.clinicasanfelipe.citas.cita.dto.CitaRequest;
import pe.clinicasanfelipe.citas.cita.dto.CitaResponse;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private static final String ROL_ADMINISTRADOR = "ROLE_ADMINISTRADOR";

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<CitaResponse> listar(Authentication authentication) {
        return citaService.listar(authentication.getName(), esAdministrador(authentication));
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id, Authentication authentication) {
        return citaService.obtener(id, authentication.getName(), esAdministrador(authentication));
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest req, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(authentication.getName(), req));
    }

    @PutMapping("/{id}")
    public CitaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody CitaRequest req,
                                    Authentication authentication) {
        return citaService.reprogramar(id, authentication.getName(), esAdministrador(authentication), req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        citaService.cancelar(id, authentication.getName(), esAdministrador(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> ROL_ADMINISTRADOR.equals(autoridad.getAuthority()));
    }
}
