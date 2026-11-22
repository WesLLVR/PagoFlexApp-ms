package cl.duocuc.usuarios;

import cl.duocuc.usuarios.controller.UsuarioController;
import cl.duocuc.usuarios.dto.ActualizarPerfilRequest;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.CrearPersonaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.PaginaResponse;
import cl.duocuc.usuarios.dto.RecuperarResponse;
import cl.duocuc.usuarios.dto.RestablecerRequest;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;

    private final UsuarioResponse camila = new UsuarioResponse(1L, "Camila Rojas", "15.834.207-3", "camila@pagoflex.cl",
            null, null, "USUARIO_FINAL", "Usuario final", null, null, null);

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    void listar_usaLosValoresPorDefecto() throws Exception {
        when(usuarioService.listar(null, null, 0, 10)).thenReturn(new PaginaResponse<>(List.of(camila), 0, 1, 1));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].correo").value("camila@pagoflex.cl"));
    }

    @Test
    void obtenerYBuscarPorRut_devuelven200() throws Exception {
        when(usuarioService.obtener(1L)).thenReturn(camila);
        when(usuarioService.buscarPorRut("15.834.207-3")).thenReturn(camila);

        mockMvc.perform(get("/api/usuarios/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/rut/15.834.207-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("15.834.207-3"));
    }

    @Test
    void obtener_usuarioQueNoExiste_devuelve404() throws Exception {
        when(usuarioService.obtener(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearPersona_devuelve201() throws Exception {
        when(usuarioService.crearPersona(any(CrearPersonaRequest.class))).thenReturn(camila);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Camila Rojas\",\"rut\":\"15.834.207-3\",\"correo\":\"camila@pagoflex.cl\",\"telefono\":\"912345678\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_devuelve200ConElUsuario() throws Exception {
        when(usuarioService.iniciarSesion(new LoginRequest("camila@pagoflex.cl", "123456"))).thenReturn(camila);

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@pagoflex.cl\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila Rojas"))
                .andExpect(jsonPath("$.rol").value("USUARIO_FINAL"));
    }

    @Test
    void recuperarYRestablecer_respondenCorrectamente() throws Exception {
        when(usuarioService.recuperar("camila@pagoflex.cl")).thenReturn(new RecuperarResponse("camila@pagoflex.cl", "123456", 15));

        mockMvc.perform(post("/api/usuarios/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@pagoflex.cl\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("123456"));
        mockMvc.perform(post("/api/usuarios/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@pagoflex.cl\",\"codigo\":\"123456\",\"nueva\":\"nueva123\"}"))
                .andExpect(status().isNoContent());

        verify(usuarioService).restablecer(new RestablecerRequest("camila@pagoflex.cl", "123456", "nueva123"));
    }

    @Test
    void actualizarPerfilYCambiarContrasena_respondenCorrectamente() throws Exception {
        when(usuarioService.actualizarPerfil(eq(1L), any(ActualizarPerfilRequest.class))).thenReturn(camila);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Camila Rojas\",\"correo\":\"camila@pagoflex.cl\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/usuarios/1/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"123456\",\"nueva\":\"nueva123\"}"))
                .andExpect(status().isNoContent());

        verify(usuarioService).cambiarContrasena(1L, new CambiarContrasenaRequest("123456", "nueva123"));
    }

    @Test
    void avatar_subirVerYQuitar() throws Exception {
        when(usuarioService.subirAvatar(eq(1L), any(MultipartFile.class))).thenReturn(camila);
        when(usuarioService.obtenerAvatar(1L)).thenReturn(new byte[]{1, 2, 3});
        when(usuarioService.eliminarAvatar(1L)).thenReturn(camila);

        mockMvc.perform(multipart("/api/usuarios/1/avatar")
                        .file(new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[]{1})))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
        mockMvc.perform(delete("/api/usuarios/1/avatar")).andExpect(status().isOk());
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());

        verify(usuarioService).eliminar(1L);
    }
}
