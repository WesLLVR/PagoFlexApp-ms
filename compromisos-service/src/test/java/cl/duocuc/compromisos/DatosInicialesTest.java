package cl.duocuc.compromisos;

import cl.duocuc.compromisos.config.DatosIniciales;
import cl.duocuc.compromisos.model.Compromiso;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.model.EstadoCompromiso;
import cl.duocuc.compromisos.repository.CompromisoRepository;
import cl.duocuc.compromisos.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private CompromisoRepository compromisoRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_conBaseVacia_cargaEmpresasYCompromisos() {
        when(empresaRepository.count()).thenReturn(0L);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(compromisoRepository.save(any(Compromiso.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        ArgumentCaptor<Compromiso> captor = ArgumentCaptor.forClass(Compromiso.class);

        datosIniciales.run();

        verify(empresaRepository, times(8)).save(any(Empresa.class));
        verify(compromisoRepository, atLeast(51)).save(captor.capture());
        List<Compromiso> guardados = captor.getAllValues();
        assertEquals("Camila Andrea Rojas Peña", guardados.get(0).getNombreUsuario());
        assertEquals(EstadoCompromiso.PAGADO, guardados.get(0).getEstado());
    }

    @Test
    void run_conDatos_noCargaNada() {
        when(empresaRepository.count()).thenReturn(8L);

        datosIniciales.run();

        verify(compromisoRepository, never()).save(any(Compromiso.class));
    }
}
