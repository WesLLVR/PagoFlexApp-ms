package cl.duocuc.usuarios.repository;

import cl.duocuc.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNombreAsc();

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByRut(String rut);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRut(String rut);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
