package pe.clinicasanfelipe.citas.cita;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasanfelipe.citas.cita.dto.CitaRequest;
import pe.clinicasanfelipe.citas.cita.dto.CitaResponse;
import pe.clinicasanfelipe.citas.exception.ApiException;
import pe.clinicasanfelipe.citas.servicio.Servicio;
import pe.clinicasanfelipe.citas.servicio.ServicioRepository;
import pe.clinicasanfelipe.citas.usuario.Usuario;
import pe.clinicasanfelipe.citas.usuario.UsuarioRepository;

@Service
public class CitaService {

    private static final Long SIN_EXCLUIR = -1L;

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;

    public CitaService(CitaRepository citaRepository, UsuarioRepository usuarioRepository,
                       ServicioRepository servicioRepository) {
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listar(String correo, boolean esAdministrador) {
        List<Cita> citas = esAdministrador
                ? citaRepository.listarTodas()
                : citaRepository.listarPorPaciente(buscarUsuario(correo).getId());
        return citas.stream().map(CitaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id, String correo, boolean esAdministrador) {
        Cita cita = buscar(id);
        validarAcceso(cita, correo, esAdministrador);
        return CitaResponse.desde(cita);
    }

    @Transactional
    public CitaResponse crear(String correo, CitaRequest req) {
        Usuario paciente = buscarUsuario(correo);
        Servicio servicio = buscarServicio(req.servicioId());
        validarDisponibilidad(servicio, req.fechaHora(), SIN_EXCLUIR);

        Cita cita = new Cita();
        cita.setUsuario(paciente);
        cita.setServicio(servicio);
        cita.setFechaHora(req.fechaHora());
        cita.setMotivo(req.motivo());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return CitaResponse.desde(citaRepository.save(cita));
    }

    @Transactional
    public CitaResponse reprogramar(Long id, String correo, boolean esAdministrador, CitaRequest req) {
        Cita cita = buscar(id);
        validarAcceso(cita, correo, esAdministrador);
        validarProgramada(cita);

        Servicio servicio = buscarServicio(req.servicioId());
        validarDisponibilidad(servicio, req.fechaHora(), cita.getId());

        cita.setServicio(servicio);
        cita.setFechaHora(req.fechaHora());
        cita.setMotivo(req.motivo());
        return CitaResponse.desde(citaRepository.save(cita));
    }

    @Transactional
    public void cancelar(Long id, String correo, boolean esAdministrador) {
        Cita cita = buscar(id);
        validarAcceso(cita, correo, esAdministrador);
        validarProgramada(cita);
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    private Cita buscar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ApiException("Cita no encontrada", HttpStatus.NOT_FOUND));
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }

    private Servicio buscarServicio(Long servicioId) {
        return servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ApiException("Servicio no encontrado", HttpStatus.NOT_FOUND));
    }

    private void validarAcceso(Cita cita, String correo, boolean esAdministrador) {
        if (!esAdministrador && !cita.getUsuario().getCorreo().equals(correo)) {
            throw new ApiException("No tienes permiso para acceder a esta cita", HttpStatus.FORBIDDEN);
        }
    }

    private void validarProgramada(Cita cita) {
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new ApiException("La cita ya fue cancelada", HttpStatus.CONFLICT);
        }
    }

    private void validarDisponibilidad(Servicio servicio, LocalDateTime fechaHora, Long excluirId) {
        if (!servicio.isActivo()) {
            throw new ApiException("El servicio no esta disponible", HttpStatus.BAD_REQUEST);
        }
        if (citaRepository.existeConflicto(servicio.getId(), fechaHora, EstadoCita.PROGRAMADA, excluirId)) {
            throw new ApiException("Ya existe una cita para ese servicio en ese horario", HttpStatus.CONFLICT);
        }
    }
}
