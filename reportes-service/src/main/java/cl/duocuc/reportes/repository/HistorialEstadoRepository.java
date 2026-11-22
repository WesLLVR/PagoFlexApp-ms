package cl.duocuc.reportes.repository;

import cl.duocuc.reportes.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByReporteIdOrderByFechaAsc(Long reporteId);
}
