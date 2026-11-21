package cl.duocuc.usuarios;

import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.CrearPersonaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.RecuperarResponse;
import cl.duocuc.usuarios.dto.RestablecerRequest;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Rol;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import cl.duocuc.usuarios.service.AlmacenamientoService;
import cl.duocuc.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario camila() {
        return Usuario.builder()
                .id(1L).nombre("Camila Rojas").rut("15.834.207-3").correo("camila@pagoflex.cl")
                .contrasena("123456").rol(Rol.USUARIO_FINAL)
                .build();
    }

    @Test
    void iniciarSesion_conClaveCorrecta_devuelveElUsuario() {
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(camila()));

        UsuarioResponse respuesta = usuarioService.iniciarSesion(new LoginRequest("camila@pagoflex.cl", "123456"));

        assertEquals("Camila Rojas", respuesta.nombre());
        assertEquals("USUARIO_FINAL", respuesta.rol());
    }

    @Test
    void iniciarSesion_conClaveIncorrecta_responde401() {
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(camila()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(new LoginRequest("camila@pagoflex.cl", "000000")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void recuperar_generaUnCodigoDeSeisDigitos() {
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(camila()));

        RecuperarResponse respuesta = usuarioService.recuperar("camila@pagoflex.cl");

        assertEquals(6, respuesta.codigo().length());
        assertEquals(15, respuesta.minutosVigencia());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void restablecer_conCodigoIncorrecto_responde400() {
        Usuario usuario = camila();
        usuario.setCodigoRecuperacion("111111");
        usuario.setVencimientoCodigo(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(usuario));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.restablecer(new RestablecerRequest("camila@pagoflex.cl", "222222", "nueva123")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void restablecer_conCodigoCorrecto_cambiaLaClaveYBorraElCodigo() {
        Usuario usuario = camila();
        usuario.setCodigoRecuperacion("111111");
        usuario.setVencimientoCodigo(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(usuario));

        usuarioService.restablecer(new RestablecerRequest("camila@pagoflex.cl", "111111", "nueva123"));

        assertEquals("nueva123", usuario.getContrasena());
        assertNull(usuario.getCodigoRecuperacion());
    }

    @Test
    void cambiarContrasena_conClaveActualIncorrecta_responde401() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(camila()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarContrasena(1L, new CambiarContrasenaRequest("999999", "nueva123")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void crearPersona_conRutRepetido_responde409() {
        when(usuarioRepository.existsByRut("15.834.207-3")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.crearPersona(new CrearPersonaRequest(
                        "Camila Rojas", "15.834.207-3", "otra@pagoflex.cl", "912345678")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }
}
