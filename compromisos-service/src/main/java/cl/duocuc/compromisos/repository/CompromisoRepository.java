package cl.duocuc.compromisos.repository;

import cl.duocuc.compromisos.model.Compromiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompromisoRepository extends JpaRepository<Compromiso, Long> {

    List<Compromiso> findByUsuarioId(Long usuarioId);

    List<Compromiso> findByEmpresaId(Long empresaId);

    List<Compromiso> findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(Long usuarioId, Long empresaId, String deuda);

    List<Compromiso> findByUsuarioIdAndEmpresaIdAndConceptoIgnoreCase(Long usuarioId, Long empresaId, String concepto);

    Optional<Compromiso> findTopByOrderByIdDesc();
}
