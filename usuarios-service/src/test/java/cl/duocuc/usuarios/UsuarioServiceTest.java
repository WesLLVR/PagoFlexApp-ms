package cl.duocuc.usuarios;

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
import cl.duocuc.usuarios.service.AlmacenamientoService;
import cl.duocuc.usuarios.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @BeforeEach
    void prepararPeticion() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarPeticion() {
        RequestContextHolder.resetRequestAttributes();
    }

    private Usuario camila() {
        return Usuario.builder()
                .id(1L).nombre("Camila Rojas").rut("15.834.207-3").correo("camila@pagoflex.cl")
                .contrasena("123456").rol(Rol.USUARIO_FINAL)
                .build();
    }

    private Usuario andres() {
        return Usuario.builder()
                .id(9L).nombre("Andrés Lagos").rut("16.248.317-K").correo("andres@pagoflex.cl")
                .contrasena("123456").rol(Rol.AGENTE).cargo("Soporte")
                .build();
    }

    private void devolverAlGuardar() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void listar_filtraPorRolYBusqueda() {
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(andres(), camila()));

        PaginaResponse<UsuarioResponse> agentes = usuarioService.listar(null, "agente", 0, 10);
        PaginaResponse<UsuarioResponse> porCorreo = usuarioService.listar("CAMILA@", null, 0, 10);

        assertEquals(1, agentes.totalElementos());
        assertEquals("Andrés Lagos", agentes.contenido().get(0).nombre());
        assertEquals("Camila Rojas", porCorreo.contenido().get(0).nombre());
    }

    @Test
    void obtener_usuarioQueNoExiste_responde404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> usuarioService.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void buscarPorRut_normalizaElRutYLoEncuentra() {
        when(usuarioRepository.findByRut("16.248.317-K")).thenReturn(Optional.of(andres()));

        UsuarioResponse respuesta = usuarioService.buscarPorRut(" 16.248.317-k ");

        assertEquals("Agente PagoFlex", respuesta.rolNombre());
    }

    @Test
    void buscarPorRut_inexistente_responde404() {
        when(usuarioRepository.findByRut("11.111.111-1")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.buscarPorRut("11.111.111-1"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearPersona_guardaUsuarioFinalSinContrasena() {
        devolverAlGuardar();

        UsuarioResponse respuesta = usuarioService.crearPersona(new CrearPersonaRequest(
                " Ana Torres Vidal ", "20.111.222-3", "Ana@PagoFlex.cl", "+56 9 1234 5678"));

        assertEquals("Ana Torres Vidal", respuesta.nombre());
        assertEquals("ana@pagoflex.cl", respuesta.correo());
        assertEquals("USUARIO_FINAL", respuesta.rol());
    }

    @Test
    void crearPersona_conDatosInvalidos_responde400() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.crearPersona(
                new CrearPersonaRequest("An", "20.111.222-3", "ana@pagoflex.cl", "912345678"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.crearPersona(
                new CrearPersonaRequest("Ana Torres", " ", "ana@pagoflex.cl", "912345678"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.crearPersona(
                new CrearPersonaRequest("Ana Torres", "20.111.222-3", "ana.pagoflex", "912345678"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.crearPersona(
                new CrearPersonaRequest("Ana Torres", "20.111.222-3", null, "912345678"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.crearPersona(
                new CrearPersonaRequest("Ana Torres", "20.111.222-3", "ana@pagoflex.cl", "1234"))).getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearPersona_conRutRepetido_responde409() {
        when(usuarioRepository.existsByRut("15.834.207-3")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.crearPersona(new CrearPersonaRequest(
                        "Camila Rojas", "15.834.207-3", "otra@pagoflex.cl", "912345678")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void crearPersona_conCorreoRepetido_responde409() {
        when(usuarioRepository.existsByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.crearPersona(new CrearPersonaRequest(
                        "Camila Rojas", "20.111.222-3", "camila@pagoflex.cl", "912345678")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
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
    void iniciarSesion_sinDatos_responde400() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(new LoginRequest("", null)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
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
    void recuperar_sinCorreoOConCorreoInexistente_falla() {
        when(usuarioRepository.findByCorreoIgnoreCase("nadie@pagoflex.cl")).thenReturn(Optional.empty());

        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> usuarioService.recuperar(" ")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> usuarioService.recuperar("nadie@pagoflex.cl")).getStatusCode());
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
    void restablecer_conCodigoVencido_responde400() {
        Usuario usuario = camila();
        usuario.setCodigoRecuperacion("111111");
        usuario.setVencimientoCodigo(LocalDateTime.now().minusMinutes(1));
        when(usuarioRepository.findByCorreoIgnoreCase("camila@pagoflex.cl")).thenReturn(Optional.of(usuario));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.restablecer(new RestablecerRequest("camila@pagoflex.cl", "111111", "nueva123")));

        assertEquals("El código venció, solicita uno nuevo", error.getReason());
    }

    @Test
    void restablecer_sinCorreo_responde404() {
        when(usuarioRepository.findByCorreoIgnoreCase("")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.restablecer(new RestablecerRequest(null, "111111", "nueva123")));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
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
    void actualizarPerfil_guardaLosDatosLimpios() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(camila()));
        devolverAlGuardar();

        UsuarioResponse respuesta = usuarioService.actualizarPerfil(1L,
                new ActualizarPerfilRequest("Camila Andrea Rojas", "CAMILA@pagoflex.cl", " ", " Maipú "));

        assertEquals("Camila Andrea Rojas", respuesta.nombre());
        assertEquals("camila@pagoflex.cl", respuesta.correo());
        assertNull(respuesta.telefono());
        assertEquals("Maipú", respuesta.comuna());
    }

    @Test
    void actualizarPerfil_conErrores_noGuarda() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(camila()));
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdNot("andres@pagoflex.cl", 1L)).thenReturn(true);

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.actualizarPerfil(1L,
                new ActualizarPerfilRequest("Ca", "camila@pagoflex.cl", null, null))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> usuarioService.actualizarPerfil(1L,
                new ActualizarPerfilRequest("Camila Rojas", "camila@pagoflex.cl", "12ab", null))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class, () -> usuarioService.actualizarPerfil(1L,
                new ActualizarPerfilRequest("Camila Rojas", "andres@pagoflex.cl", "+56 9 1234 5678", null))).getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarContrasena_conClaveActualIncorrecta_responde401() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(camila()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarContrasena(1L, new CambiarContrasenaRequest("999999", "nueva123")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void cambiarContrasena_igualALaActualOCorta_responde400() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(camila()));

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarContrasena(1L, new CambiarContrasenaRequest("123456", "123456"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarContrasena(1L, new CambiarContrasenaRequest("123456", "abc"))).getStatusCode());
    }

    @Test
    void cambiarContrasena_correcta_guardaLaNueva() {
        Usuario usuario = camila();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.cambiarContrasena(1L, new CambiarContrasenaRequest("123456", "pagoflex1"));

        assertEquals("pagoflex1", usuario.getContrasena());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void subirAvatar_guardaLaFotoNuevaYBorraLaAnterior() {
        Usuario usuario = camila();
        usuario.setAvatar("vieja.jpg");
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[]{1, 2});
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(almacenamientoService.guardar(foto)).thenReturn("nueva.jpg");
        devolverAlGuardar();

        UsuarioResponse respuesta = usuarioService.subirAvatar(1L, foto);

        assertTrue(respuesta.avatarUrl().endsWith("/api/usuarios/1/avatar?v=nueva.jpg"));
        verify(almacenamientoService).eliminar("vieja.jpg");
    }

    @Test
    void obtenerAvatar_devuelveLosBytesOResponde404() {
        Usuario conFoto = camila();
        conFoto.setAvatar("foto.jpg");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(conFoto));
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(andres()));
        when(almacenamientoService.leer("foto.jpg")).thenReturn(new byte[]{7});

        assertArrayEquals(new byte[]{7}, usuarioService.obtenerAvatar(1L));
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> usuarioService.obtenerAvatar(9L)).getStatusCode());
    }

    @Test
    void eliminarAvatar_quitaLaFoto() {
        Usuario usuario = camila();
        usuario.setAvatar("foto.jpg");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        devolverAlGuardar();

        UsuarioResponse respuesta = usuarioService.eliminarAvatar(1L);

        assertNull(respuesta.avatarUrl());
        verify(almacenamientoService).eliminar("foto.jpg");
    }

    @Test
    void eliminar_borraElUsuario() {
        Usuario usuario = camila();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        verify(usuarioRepository).delete(usuario);
    }
}
