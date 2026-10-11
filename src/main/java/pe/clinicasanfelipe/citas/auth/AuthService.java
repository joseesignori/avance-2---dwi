package pe.clinicasanfelipe.citas.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.clinicasanfelipe.citas.auth.dto.AuthResponse;
import pe.clinicasanfelipe.citas.auth.dto.LoginRequest;
import pe.clinicasanfelipe.citas.auth.dto.RegisterRequest;
import pe.clinicasanfelipe.citas.exception.ApiException;
import pe.clinicasanfelipe.citas.security.JwtUtil;
import pe.clinicasanfelipe.citas.usuario.Rol;
import pe.clinicasanfelipe.citas.usuario.Usuario;
import pe.clinicasanfelipe.citas.usuario.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new ApiException("El correo ya esta registrado", HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setCorreo(req.correo());
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        usuario.setTelefono(req.telefono());
        usuario.setRol(Rol.PACIENTE);
        usuarioRepository.save(usuario);

        return construirRespuesta(usuario);
    }

    public AuthResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new ApiException("Credenciales invalidas", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.password(), usuario.getPasswordHash())) {
            throw new ApiException("Credenciales invalidas", HttpStatus.UNAUTHORIZED);
        }

        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario usuario) {
        String token = jwtUtil.generarToken(usuario);
        return new AuthResponse(token, usuario.getNombre(), usuario.getCorreo(), usuario.getRol().name());
    }
}
