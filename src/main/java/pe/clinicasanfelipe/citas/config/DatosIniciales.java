package pe.clinicasanfelipe.citas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import pe.clinicasanfelipe.citas.servicio.Servicio;
import pe.clinicasanfelipe.citas.servicio.ServicioRepository;
import pe.clinicasanfelipe.citas.usuario.Rol;
import pe.clinicasanfelipe.citas.usuario.Usuario;
import pe.clinicasanfelipe.citas.usuario.UsuarioRepository;

@Configuration
public class DatosIniciales {

    @Bean
    public CommandLineRunner crearAdministrador(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String correo = "admin@clinicasanfelipe.pe";
            if (!usuarioRepository.existsByCorreo(correo)) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setCorreo(correo);
                admin.setPasswordHash(passwordEncoder.encode("admin12345"));
                admin.setRol(Rol.ADMINISTRADOR);
                usuarioRepository.save(admin);
            }
        };
    }

    @Bean
    public CommandLineRunner crearServicios(ServicioRepository servicioRepository) {
        return args -> {
            if (servicioRepository.count() == 0) {
                crear(servicioRepository, "Medicina General", "Consulta medica general", 30, "60.00");
                crear(servicioRepository, "Pediatria", "Atencion medica para ninos y adolescentes", 30, "80.00");
                crear(servicioRepository, "Cardiologia", "Evaluacion y control cardiologico", 45, "120.00");
            }
        };
    }

    private void crear(ServicioRepository repository, String nombre, String descripcion,
                       int duracionMinutos, String precio) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setPrecio(new BigDecimal(precio));
        repository.save(servicio);
    }
}
