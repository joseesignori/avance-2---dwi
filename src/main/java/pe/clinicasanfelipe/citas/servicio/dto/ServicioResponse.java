package pe.clinicasanfelipe.citas.servicio.dto;

import java.math.BigDecimal;
import pe.clinicasanfelipe.citas.servicio.Servicio;

public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        Integer duracionMinutos,
        BigDecimal precio,
        boolean activo
) {

    public static ServicioResponse desde(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracionMinutos(),
                servicio.getPrecio(),
                servicio.isActivo()
        );
    }
}
