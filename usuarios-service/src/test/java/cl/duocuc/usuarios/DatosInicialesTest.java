package cl.duocuc.usuarios;

import cl.duocuc.usuarios.config.DatosIniciales;
import cl.duocuc.usuarios.model.Rol;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    @SuppressWarnings("unchecked")
    void run_conBaseVacia_cargaLasCuentasDePrueba() {
        when(usuarioRepository.count()).thenReturn(0L);
        ArgumentCaptor<List<Usuario>> captor = ArgumentCaptor.forClass(List.class);

        datosIniciales.run();

        verify(usuarioRepository).saveAll(captor.capture());
        List<Usuario> usuarios = captor.getValue();
        assertEquals(20, usuarios.size());
        assertEquals(8, usuarios.stream().filter(u -> u.getRol() == Rol.USUARIO_FINAL).count());
        assertEquals("123456", usuarios.get(0).getContrasena());
    }

    @Test
    void run_conDatos_noCargaNada() {
        when(usuarioRepository.count()).thenReturn(20L);

        datosIniciales.run();

        verify(usuarioRepository, never()).saveAll(anyList());
    }
}
