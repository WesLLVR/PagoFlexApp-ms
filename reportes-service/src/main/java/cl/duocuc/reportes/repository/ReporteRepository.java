package cl.duocuc.reportes.repository;

import cl.duocuc.reportes.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByUsuarioId(Long usuarioId);

    Optional<Reporte> findTopByCompromisoIdOrderByIdDesc(Long compromisoId);
}
