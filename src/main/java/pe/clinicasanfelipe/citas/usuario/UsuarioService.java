package pe.clinicasanfelipe.citas.usuario;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasanfelipe.citas.cita.CitaRepository;
import pe.clinicasanfelipe.citas.exception.ApiException;
import pe.clinicasanfelipe.citas.usuario.dto.UsuarioCreateRequest;
import pe.clinicasanfelipe.citas.usuario.dto.UsuarioResponse;
import pe.clinicasanfelipe.citas.usuario.dto.UsuarioUpdateRequest;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, CitaRepository citaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse miPerfil(String correo) {
        return UsuarioResponse.desde(usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest req) {
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new ApiException("El correo ya esta registrado", HttpStatus.CONFLICT);
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setCorreo(req.correo());
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        usuario.setTelefono(req.telefono());
        usuario.setRol(req.rol());
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest req) {
        Usuario usuario = buscar(id);
        if (!usuario.getCorreo().equals(req.correo()) && usuarioRepository.existsByCorreo(req.correo())) {
            throw new ApiException("El correo ya esta registrado", HttpStatus.CONFLICT);
        }
        usuario.setNombre(req.nombre());
        usuario.setCorreo(req.correo());
        usuario.setTelefono(req.telefono());
        usuario.setRol(req.rol());
        if (req.password() != null) {
            usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String correoActual) {
        Usuario usuario = buscar(id);
        if (usuario.getCorreo().equals(correoActual)) {
            throw new ApiException("No puedes eliminar tu propia cuenta", HttpStatus.CONFLICT);
        }
        if (citaRepository.existsByUsuarioId(id)) {
            throw new ApiException("El usuario tiene citas registradas y no puede eliminarse", HttpStatus.CONFLICT);
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }
}
