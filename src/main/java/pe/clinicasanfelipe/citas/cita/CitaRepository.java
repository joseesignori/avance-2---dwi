package pe.clinicasanfelipe.citas.cita;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT c FROM Cita c JOIN FETCH c.usuario JOIN FETCH c.servicio ORDER BY c.fechaHora")
    List<Cita> listarTodas();

    @Query("SELECT c FROM Cita c JOIN FETCH c.usuario JOIN FETCH c.servicio "
            + "WHERE c.usuario.id = :usuarioId ORDER BY c.fechaHora")
    List<Cita> listarPorPaciente(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.servicio.id = :servicioId "
            + "AND c.fechaHora = :fechaHora AND c.estado = :estado AND c.id <> :excluirId")
    boolean existeConflicto(@Param("servicioId") Long servicioId,
                            @Param("fechaHora") LocalDateTime fechaHora,
                            @Param("estado") EstadoCita estado,
                            @Param("excluirId") Long excluirId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByServicioId(Long servicioId);
}
