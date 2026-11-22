package cl.duocuc.reportes;

import cl.duocuc.reportes.config.DatosIniciales;
import cl.duocuc.reportes.model.EstadoReporte;
import cl.duocuc.reportes.model.HistorialEstado;
import cl.duocuc.reportes.model.Reporte;
import cl.duocuc.reportes.repository.HistorialEstadoRepository;
import cl.duocuc.reportes.repository.ReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_conBaseVacia_cargaReportesConSuHistorial() {
        when(reporteRepository.count()).thenReturn(0L);
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        ArgumentCaptor<Reporte> reportes = ArgumentCaptor.forClass(Reporte.class);

        datosIniciales.run();

        verify(reporteRepository, atLeastOnce()).save(reportes.capture());
        verify(historialRepository, atLeastOnce()).save(any(HistorialEstado.class));
        List<Reporte> guardados = reportes.getAllValues();
        assertEquals("REP-0001", guardados.get(0).getNumero());
        assertTrue(guardados.stream().anyMatch(r -> r.getEstado() == EstadoReporte.ABIERTO));
    }

    @Test
    void run_conDatos_noCargaNada() {
        when(reporteRepository.count()).thenReturn(9L);

        datosIniciales.run();

        verify(reporteRepository, never()).save(any(Reporte.class));
    }
}
