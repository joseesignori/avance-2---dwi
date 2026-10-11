package pe.clinicasanfelipe.citas.cita.dto;

import java.time.LocalDateTime;
import pe.clinicasanfelipe.citas.cita.Cita;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long servicioId,
        String servicioNombre,
        LocalDateTime fechaHora,
        String estado,
        String motivo
) {

    public static CitaResponse desde(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getUsuario().getId(),
                cita.getUsuario().getNombre(),
                cita.getServicio().getId(),
                cita.getServicio().getNombre(),
                cita.getFechaHora(),
                cita.getEstado().name(),
                cita.getMotivo()
        );
    }
}
