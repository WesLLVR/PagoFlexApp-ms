package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.dto.ActualizarPerfilRequest;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.CrearPersonaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.PaginaResponse;
import cl.duocuc.usuarios.dto.RecuperarResponse;
import cl.duocuc.usuarios.dto.RestablecerRequest;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Rol;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import cl.duocuc.usuarios.util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final int MINUTOS_VIGENCIA_CODIGO = 15;
    private static final int LARGO_MINIMO_CONTRASENA = 6;

    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamientoService;
    private final SecureRandom aleatorio = new SecureRandom();

    public PaginaResponse<UsuarioResponse> listar(String buscar, String rol, int pagina, int tamano) {
        List<UsuarioResponse> filtrados = usuarioRepository.findAllByOrderByNombreAsc().stream()
                .filter(u -> vacio(rol) || u.getRol().name().equalsIgnoreCase(rol))
                .filter(u -> vacio(buscar) || contiene(u.getNombre(), buscar) || contiene(u.getRut(), buscar)
                        || contiene(u.getCorreo(), buscar))
                .map(this::aRespuesta)
                .toList();
        return Paginacion.paginar(filtrados, pagina, tamano);
    }

    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public UsuarioResponse buscarPorRut(String rut) {
        return usuarioRepository.findByRut(rut.trim().toUpperCase(Locale.ROOT))
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una persona con ese RUT"));
    }

    public UsuarioResponse crearPersona(CrearPersonaRequest datos) {
        if (vacio(datos.nombre()) || datos.nombre().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe el nombre completo");
        }
        if (vacio(datos.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUT es obligatorio");
        }
        validarCorreo(datos.correo());
        validarTelefono(datos.telefono());
        String rut = datos.rut().trim().toUpperCase(Locale.ROOT);
        if (usuarioRepository.existsByRut(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese RUT ya está registrado");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(datos.correo().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        Usuario nuevo = Usuario.builder()
                .nombre(datos.nombre().trim())
                .rut(rut)
                .correo(datos.correo().trim().toLowerCase(Locale.ROOT))
                .telefono(datos.telefono().trim())
                .rol(Rol.USUARIO_FINAL)
                .build();
        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public UsuarioResponse iniciarSesion(LoginRequest datos) {
        if (vacio(datos.correo()) || vacio(datos.contrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa tu correo y tu contraseña");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(datos.correo().trim())
                .filter(u -> u.getContrasena() != null && u.getContrasena().equals(datos.contrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return aRespuesta(usuario);
    }

    public RecuperarResponse recuperar(String correo) {
        if (vacio(correo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa tu correo");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una cuenta con ese correo"));

        String codigo = String.format(Locale.ROOT, "%06d", aleatorio.nextInt(1_000_000));
        usuario.setCodigoRecuperacion(codigo);
        usuario.setVencimientoCodigo(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_CODIGO));
        usuarioRepository.save(usuario);
        return new RecuperarResponse(usuario.getCorreo(), codigo, MINUTOS_VIGENCIA_CODIGO);
    }

    public void restablecer(RestablecerRequest datos) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(datos.correo() == null ? "" : datos.correo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una cuenta con ese correo"));

        if (usuario.getCodigoRecuperacion() == null || !usuario.getCodigoRecuperacion().equals(datos.codigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código no es válido");
        }
        if (usuario.getVencimientoCodigo().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código venció, solicita uno nuevo");
        }
        validarContrasenaNueva(datos.nueva());

        usuario.setContrasena(datos.nueva());
        usuario.setCodigoRecuperacion(null);
        usuario.setVencimientoCodigo(null);
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse actualizarPerfil(Long id, ActualizarPerfilRequest datos) {
        Usuario usuario = buscar(id);

        if (vacio(datos.nombre()) || datos.nombre().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe el nombre completo");
        }
        validarCorreo(datos.correo());
        if (!vacio(datos.telefono())) {
            validarTelefono(datos.telefono());
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(datos.correo().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(datos.correo().trim().toLowerCase(Locale.ROOT));
        usuario.setTelefono(vacio(datos.telefono()) ? null : datos.telefono().trim());
        usuario.setComuna(vacio(datos.comuna()) ? null : datos.comuna().trim());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest datos) {
        Usuario usuario = buscar(id);

        if (usuario.getContrasena() == null || !usuario.getContrasena().equals(datos.actual())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }
        validarContrasenaNueva(datos.nueva());
        if (datos.nueva().equals(datos.actual())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta a la actual");
        }

        usuario.setContrasena(datos.nueva());
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse subirAvatar(Long id, MultipartFile archivo) {
        Usuario usuario = buscar(id);
        String anterior = usuario.getAvatar();

        usuario.setAvatar(almacenamientoService.guardar(archivo));
        Usuario guardado = usuarioRepository.save(usuario);

        almacenamientoService.eliminar(anterior);
        return aRespuesta(guardado);
    }

    public byte[] obtenerAvatar(Long id) {
        Usuario usuario = buscar(id);
        if (usuario.getAvatar() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene foto");
        }
        return almacenamientoService.leer(usuario.getAvatar());
    }

    public UsuarioResponse eliminarAvatar(Long id) {
        Usuario usuario = buscar(id);
        String anterior = usuario.getAvatar();
        usuario.setAvatar(null);
        Usuario guardado = usuarioRepository.save(usuario);
        almacenamientoService.eliminar(anterior);
        return aRespuesta(guardado);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        usuarioRepository.delete(usuario);
        almacenamientoService.eliminar(usuario.getAvatar());
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarCorreo(String correo) {
        if (vacio(correo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
        }
        if (!correo.contains("@") || !correo.substring(correo.indexOf('@')).contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo válido");
        }
    }

    private void validarTelefono(String telefono) {
        String digitos = telefono == null ? "" : telefono.replace(" ", "").replace("+56", "");
        if (digitos.length() != 9 || !digitos.chars().allMatch(Character::isDigit)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El teléfono debe tener 9 dígitos");
        }
    }

    private void validarContrasenaNueva(String nueva) {
        if (nueva == null || nueva.length() < LARGO_MINIMO_CONTRASENA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRut(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getComuna(),
                usuario.getRol().name(),
                usuario.getRol().getEtiqueta(),
                usuario.getEmpresaId(),
                usuario.getCargo(),
                urlAvatar(usuario)
        );
    }

    private String urlAvatar(Usuario usuario) {
        if (usuario.getAvatar() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/" + usuario.getId() + "/avatar")
                .queryParam("v", usuario.getAvatar())
                .toUriString();
    }

    private boolean contiene(String texto, String buscado) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(buscado.trim().toLowerCase(Locale.ROOT));
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
