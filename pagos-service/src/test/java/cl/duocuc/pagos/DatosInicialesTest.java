package cl.duocuc.pagos;

import cl.duocuc.pagos.config.DatosIniciales;
import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.repository.PagoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_conBaseVacia_cargaElHistorialDePagos() {
        when(pagoRepository.count()).thenReturn(0L);
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);

        datosIniciales.run();

        verify(pagoRepository, atLeast(37)).save(captor.capture());
        List<Pago> pagos = captor.getAllValues();
        assertEquals("CPR-000300", pagos.get(0).getNumeroComprobante());
        assertEquals(1L, pagos.get(0).getUsuarioId());
    }

    @Test
    void run_conDatos_noCargaNada() {
        when(pagoRepository.count()).thenReturn(37L);

        datosIniciales.run();

        verify(pagoRepository, never()).save(any(Pago.class));
    }
}
