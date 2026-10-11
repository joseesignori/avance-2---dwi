package pe.clinicasanfelipe.citas.cita.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CitaRequest(
        @NotNull(message = "El servicio es obligatorio")
        Long servicioId,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha y hora deben ser futuras")
        LocalDateTime fechaHora,

        @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
        String motivo
) {
}
