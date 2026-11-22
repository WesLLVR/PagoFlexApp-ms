package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.ActualizarPerfilRequest;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.CrearPersonaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.PaginaResponse;
import cl.duocuc.usuarios.dto.RecuperarRequest;
import cl.duocuc.usuarios.dto.RecuperarResponse;
import cl.duocuc.usuarios.dto.RestablecerRequest;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Usuarios", description = "Cuentas, inicio de sesión, recuperación de clave y perfil")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Lista los usuarios con búsqueda, filtro por rol y paginación")
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String rol,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return usuarioService.listar(buscar, rol, pagina, tamano);
    }

    @Operation(summary = "Obtiene un usuario por su id")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Busca una persona por su RUT")
    @GetMapping("/rut/{rut}")
    public UsuarioResponse buscarPorRut(@PathVariable String rut) {
        return usuarioService.buscarPorRut(rut);
    }

    @Operation(summary = "Crea una persona sin contraseña (lo usa compromisos-service)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearPersona(@RequestBody CrearPersonaRequest datos) {
        return usuarioService.crearPersona(datos);
    }

    @Operation(summary = "Inicia sesión con correo y contraseña")
    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@RequestBody LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }

    @Operation(summary = "Genera un código de 6 dígitos para recuperar la contraseña")
    @PostMapping("/recuperar")
    public RecuperarResponse recuperar(@RequestBody RecuperarRequest datos) {
        return usuarioService.recuperar(datos.correo());
    }

    @Operation(summary = "Crea una nueva contraseña usando el código de recuperación")
    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@RequestBody RestablecerRequest datos) {
        usuarioService.restablecer(datos);
    }

    @Operation(summary = "Actualiza nombre, correo, teléfono y comuna")
    @PutMapping("/{id}")
    public UsuarioResponse actualizarPerfil(@PathVariable Long id, @RequestBody ActualizarPerfilRequest datos) {
        return usuarioService.actualizarPerfil(id, datos);
    }

    @Operation(summary = "Cambia la contraseña validando la actual")
    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @Operation(summary = "Sube la foto de perfil")
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse subirAvatar(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return usuarioService.subirAvatar(id, archivo);
    }

    @Operation(summary = "Descarga la foto de perfil")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuarioService.obtenerAvatar(id));
    }

    @Operation(summary = "Quita la foto de perfil")
    @DeleteMapping("/{id}/avatar")
    public UsuarioResponse eliminarAvatar(@PathVariable Long id) {
        return usuarioService.eliminarAvatar(id);
    }

    @Operation(summary = "Elimina un usuario")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
