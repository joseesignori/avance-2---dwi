package pe.clinicasanfelipe.citas.servicio;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasanfelipe.citas.cita.CitaRepository;
import pe.clinicasanfelipe.citas.exception.ApiException;
import pe.clinicasanfelipe.citas.servicio.dto.ServicioRequest;
import pe.clinicasanfelipe.citas.servicio.dto.ServicioResponse;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;

    public ServicioService(ServicioRepository servicioRepository, CitaRepository citaRepository) {
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(String nombre) {
        List<Servicio> servicios = (nombre == null || nombre.isBlank())
                ? servicioRepository.findAll(Sort.by("nombre"))
                : servicioRepository.buscarPorNombre(nombre.trim());
        return servicios.stream().map(ServicioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return ServicioResponse.desde(buscar(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest req) {
        if (servicioRepository.findByNombreIgnoreCase(req.nombre().trim()).isPresent()) {
            throw new ApiException("Ya existe un servicio con ese nombre", HttpStatus.CONFLICT);
        }
        Servicio servicio = new Servicio();
        aplicar(servicio, req);
        return ServicioResponse.desde(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest req) {
        Servicio servicio = buscar(id);
        boolean nombreEnUso = servicioRepository.findByNombreIgnoreCase(req.nombre().trim())
                .filter(otro -> !otro.getId().equals(id))
                .isPresent();
        if (nombreEnUso) {
            throw new ApiException("Ya existe un servicio con ese nombre", HttpStatus.CONFLICT);
        }
        aplicar(servicio, req);
        return ServicioResponse.desde(servicioRepository.save(servicio));
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = buscar(id);
        if (citaRepository.existsByServicioId(id)) {
            throw new ApiException(
                    "El servicio tiene citas asociadas; desactivalo en lugar de eliminarlo",
                    HttpStatus.CONFLICT);
        }
        servicioRepository.delete(servicio);
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ApiException("Servicio no encontrado", HttpStatus.NOT_FOUND));
    }

    private void aplicar(Servicio servicio, ServicioRequest req) {
        servicio.setNombre(req.nombre().trim());
        servicio.setDescripcion(req.descripcion());
        servicio.setDuracionMinutos(req.duracionMinutos());
        servicio.setPrecio(req.precio());
        servicio.setActivo(req.activo() == null || req.activo());
    }
}
