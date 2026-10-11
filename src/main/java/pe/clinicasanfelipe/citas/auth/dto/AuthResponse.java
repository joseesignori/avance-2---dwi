package pe.clinicasanfelipe.citas.auth.dto;

public record AuthResponse(
        String token,
        String nombre,
        String correo,
        String rol
) {
}
