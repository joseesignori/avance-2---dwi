package pe.clinicasanfelipe.citas.servicio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByNombreIgnoreCase(String nombre);

    @Query("SELECT s FROM Servicio s WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY s.nombre")
    List<Servicio> buscarPorNombre(@Param("nombre") String nombre);
}
