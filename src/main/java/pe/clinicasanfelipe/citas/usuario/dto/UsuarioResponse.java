package pe.clinicasanfelipe.citas.usuario.dto;

import pe.clinicasanfelipe.citas.usuario.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String rol
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol().name()
        );
    }
}
