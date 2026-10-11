package pe.clinicasanfelipe.citas.servicio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record   ServicioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripcion no puede superar los 255 caracteres")
        String descripcion,

        @NotNull(message = "La duracion es obligatoria")
        @Positive(message = "La duracion debe ser mayor a 0")
        Integer duracionMinutos,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precio,

        Boolean activo
) {
}
