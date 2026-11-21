package cl.duocuc.usuarios;

import cl.duocuc.usuarios.controller.UsuarioController;
import cl.duocuc.usuarios.dto.LoginRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    void login_devuelve200ConElUsuario() throws Exception {
        UsuarioResponse camila = new UsuarioResponse(1L, "Camila Rojas", "15.834.207-3", "camila@pagoflex.cl",
                null, null, "USUARIO_FINAL", "Usuario final", null, null, null);
        when(usuarioService.iniciarSesion(new LoginRequest("camila@pagoflex.cl", "123456"))).thenReturn(camila);

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@pagoflex.cl\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila Rojas"))
                .andExpect(jsonPath("$.rol").value("USUARIO_FINAL"));
    }

    @Test
    void obtener_usuarioQueNoExiste_devuelve404() throws Exception {
        when(usuarioService.obtener(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}
