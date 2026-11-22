package cl.duocuc.pagos.repository;

import cl.duocuc.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    List<Pago> findAllByOrderByFechaHoraDesc();

    Optional<Pago> findByCompromisoId(Long compromisoId);

    boolean existsByCompromisoId(Long compromisoId);
}
