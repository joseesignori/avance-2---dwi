package pe.clinicasanfelipe.citas.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.clinicasanfelipe.citas.usuario.Rol;

public record UsuarioCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "La contrasena debe tener al menos 8 caracteres, con letras y numeros")
        String password,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String telefono,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
