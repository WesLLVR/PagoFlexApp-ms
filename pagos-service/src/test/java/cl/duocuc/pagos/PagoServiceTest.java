package cl.duocuc.pagos;

import cl.duocuc.pagos.client.CompromisoClient;
import cl.duocuc.pagos.dto.CompromisoDto;
import cl.duocuc.pagos.dto.CumplimientoResponse;
import cl.duocuc.pagos.dto.PagableDto;
import cl.duocuc.pagos.dto.PaginaResponse;
import cl.duocuc.pagos.dto.PagoRealizadoResponse;
import cl.duocuc.pagos.dto.PagoRequest;
import cl.duocuc.pagos.dto.PagoResponse;
import cl.duocuc.pagos.model.Canal;
import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.repository.PagoRepository;
import cl.duocuc.pagos.service.PagoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private CompromisoClient compromisoClient;

    @InjectMocks
    private PagoService pagoService;

    private CompromisoDto cuota(Long id, int numero, LocalDate vencimiento, String estado) {
        return new CompromisoDto(id, "CP-" + id, 1L, "Camila Rojas", "15.834.207-3", 6L, "Club Los Halcones",
                "Club Deportivo", "Cuota social", "Cuota social", numero, 12, 15000, 0, 15000, vencimiento, estado, 0);
    }

    @Test
    void pagar_devuelveElComprobanteYLaSiguienteCuota() {
        CompromisoDto cuota10 = cuota(10L, 10, LocalDate.now().plusDays(2), "PENDIENTE");
        CompromisoDto cuota11 = cuota(11L, 11, LocalDate.now().plusDays(32), "PENDIENTE");
        when(compromisoClient.obtener(10L)).thenReturn(cuota10);
        when(compromisoClient.pagable(10L)).thenReturn(new PagableDto(true, null));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(compromisoClient.cuotas(10L)).thenReturn(List.of(cuota10, cuota11));

        PagoRealizadoResponse respuesta = pagoService.pagar(new PagoRequest(10L, "Transferencia"));

        assertEquals("CPR-000001", respuesta.pago().numeroComprobante());
        assertEquals(true, respuesta.pago().aTiempo());
        assertEquals(11, respuesta.siguienteCuota().numeroCuota());
        verify(compromisoClient).marcarPagado(10L);
    }

    @Test
    void pagar_sinMedioDePago_responde400() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> pagoService.pagar(new PagoRequest(10L, null)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(compromisoClient, never()).obtener(any());
    }

    @Test
    void pagar_compromisoBloqueado_responde409YNoGuarda() {
        when(compromisoClient.obtener(11L)).thenReturn(cuota(11L, 11, LocalDate.now().plusDays(32), "PENDIENTE"));
        when(compromisoClient.pagable(11L)).thenReturn(new PagableDto(false, "Primero paga la cuota 10/12"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> pagoService.pagar(new PagoRequest(11L, "Transferencia")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void pagar_siCompromisosFalla_borraElPagoGuardado() {
        when(compromisoClient.obtener(10L)).thenReturn(cuota(10L, 10, LocalDate.now(), "PENDIENTE"));
        when(compromisoClient.pagable(10L)).thenReturn(new PagableDto(true, null));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No disponible"))
                .when(compromisoClient).marcarPagado(10L);

        assertThrows(ResponseStatusException.class, () -> pagoService.pagar(new PagoRequest(10L, "Transferencia")));

        verify(pagoRepository).delete(any(Pago.class));
    }

    @Test
    void cumplimiento_tresDeCuatroATiempo_esEnCamino() {
        when(compromisoClient.considerados(1L)).thenReturn(List.of(
                cuota(1L, 1, LocalDate.now().minusMonths(4), "PAGADO"),
                cuota(2L, 2, LocalDate.now().minusMonths(3), "PAGADO"),
                cuota(3L, 3, LocalDate.now().minusMonths(2), "PAGADO"),
                cuota(4L, 4, LocalDate.now().minusMonths(1), "PAGADO")));
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(
                pago(1L, true), pago(2L, true), pago(3L, true), pago(4L, false)));

        CumplimientoResponse respuesta = pagoService.cumplimiento(1L);

        assertEquals(75.0, respuesta.porcentaje());
        assertEquals("EN_CAMINO", respuesta.categoria());
    }

    private Pago pagoCompleto(Long id, String empresa, String concepto, LocalDateTime fechaHora) {
        return Pago.builder().id(id).numeroComprobante("CPR-0003" + id).compromisoId(id).usuarioId(1L)
                .nombreUsuario("Camila Rojas").rutUsuario("15.834.207-3").folio("CP-2026-00" + id).empresa(empresa)
                .razonSocial(empresa).concepto(concepto).montoCuota(15000).vencimiento(fechaHora.toLocalDate())
                .fechaHora(fechaHora).montoPagado(15000).canal(Canal.APP).medioPago("Transferencia").aTiempo(true)
                .build();
    }

    @Test
    void pagar_sinCompromisoOYaPagado_noLlamaACompromisos() {
        when(pagoRepository.existsByCompromisoId(10L)).thenReturn(true);

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> pagoService.pagar(new PagoRequest(null, "Transferencia"))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> pagoService.pagar(new PagoRequest(10L, "Tarjeta de débito"))).getStatusCode());
        verify(compromisoClient, never()).obtener(any());
    }

    @Test
    void pagar_atrasadoYSinMasCuotas_quedaFueraDePlazoYSinSiguiente() {
        CompromisoDto vencida = cuota(10L, 12, LocalDate.now().minusDays(3), "VENCIDO");
        when(compromisoClient.obtener(10L)).thenReturn(vencida);
        when(compromisoClient.pagable(10L)).thenReturn(new PagableDto(true, null));
        when(pagoRepository.findAll()).thenReturn(List.of(pago(453L, true)));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(compromisoClient.cuotas(10L)).thenReturn(List.of(vencida, cuota(9L, 11, LocalDate.now().minusMonths(1), "PAGADO")));

        PagoRealizadoResponse respuesta = pagoService.pagar(new PagoRequest(10L, "Tarjeta de crédito"));

        assertEquals("CPR-000454", respuesta.pago().numeroComprobante());
        assertFalse(respuesta.pago().aTiempo());
        assertNull(respuesta.siguienteCuota());
    }

    @Test
    void listar_filtraPorEmpresaFechasYBusqueda() {
        LocalDateTime ahora = LocalDateTime.now();
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(
                pagoCompleto(1L, "Club Los Halcones", "Cuota social", ahora),
                pagoCompleto(2L, "Plaza Oriente", "Arriendo", ahora.minusDays(10)),
                pagoCompleto(3L, "Club Los Halcones", "Cuota social", ahora.minusYears(1))));

        PaginaResponse<PagoResponse> club = pagoService.listar(1L, "club los halcones", ahora.getYear(), null, null, null, 0, 5);
        PaginaResponse<PagoResponse> rango = pagoService.listar(1L, null, null, ahora.toLocalDate().minusDays(15),
                ahora.toLocalDate().minusDays(5), "arriendo", 0, 5);

        assertEquals(List.of(1L), club.contenido().stream().map(PagoResponse::id).toList());
        assertEquals(List.of(2L), rango.contenido().stream().map(PagoResponse::id).toList());
    }

    @Test
    void listar_sinUsuario_usaTodosLosPagos() {
        when(pagoRepository.findAllByOrderByFechaHoraDesc()).thenReturn(List.of(pagoCompleto(1L, "Club", "Cuota", LocalDateTime.now())));

        assertEquals(1, pagoService.listar(null, "", null, null, null, " ", 0, 5).totalElementos());
    }

    @Test
    void empresas_sinRepetirYOrdenadas() {
        LocalDateTime ahora = LocalDateTime.now();
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(
                pagoCompleto(1L, "Plaza Oriente", "Arriendo", ahora),
                pagoCompleto(2L, "Club Los Halcones", "Cuota", ahora),
                pagoCompleto(3L, "Plaza Oriente", "Arriendo", ahora)));

        assertEquals(List.of("Club Los Halcones", "Plaza Oriente"), pagoService.empresas(1L));
    }

    @Test
    void obtenerYPorCompromiso_devuelvenElPagoOResponden404() {
        Pago pago = pagoCompleto(1L, "Club", "Cuota", LocalDateTime.now());
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.findById(2L)).thenReturn(Optional.empty());
        when(pagoRepository.findByCompromisoId(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.findByCompromisoId(2L)).thenReturn(Optional.empty());

        assertEquals("CPR-00031", pagoService.obtener(1L).numeroComprobante());
        assertEquals("App", pagoService.porCompromiso(1L).canalNombre());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, () -> pagoService.obtener(2L)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, () -> pagoService.porCompromiso(2L)).getStatusCode());
    }

    @Test
    void cumplimiento_segunElPorcentaje() {
        List<CompromisoDto> cuatro = List.of(
                cuota(1L, 1, LocalDate.now().minusMonths(4), "PAGADO"),
                cuota(2L, 2, LocalDate.now().minusMonths(3), "PAGADO"),
                cuota(3L, 3, LocalDate.now().minusMonths(2), "PAGADO"),
                cuota(4L, 4, LocalDate.now().minusMonths(1), "PAGADO"));
        when(compromisoClient.considerados(1L)).thenReturn(cuatro);
        when(compromisoClient.considerados(2L)).thenReturn(cuatro.subList(0, 2));
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(
                pago(1L, true), pago(2L, true), pago(3L, true), pago(4L, true)));
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(2L)).thenReturn(List.of());

        CumplimientoResponse ejemplar = pagoService.cumplimiento(1L);
        CumplimientoResponse sinHistorial = pagoService.cumplimiento(2L);

        assertEquals("EJEMPLAR", ejemplar.categoria());
        assertEquals("SIN_HISTORIAL", sinHistorial.categoria());
        assertNull(sinHistorial.porcentaje());
    }

    @Test
    void cumplimiento_entre80y95_esAlDia() {
        List<CompromisoDto> cinco = List.of(
                cuota(1L, 1, LocalDate.now().minusMonths(5), "PAGADO"),
                cuota(2L, 2, LocalDate.now().minusMonths(4), "PAGADO"),
                cuota(3L, 3, LocalDate.now().minusMonths(3), "PAGADO"),
                cuota(4L, 4, LocalDate.now().minusMonths(2), "PAGADO"),
                cuota(5L, 5, LocalDate.now().minusMonths(1), "VENCIDO"));
        when(compromisoClient.considerados(1L)).thenReturn(cinco);
        when(pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(1L)).thenReturn(List.of(
                pago(1L, true), pago(2L, true), pago(3L, true), pago(4L, true)));

        CumplimientoResponse respuesta = pagoService.cumplimiento(1L);

        assertEquals(80.0, respuesta.porcentaje());
        assertEquals("AL_DIA", respuesta.categoria());
    }

    private Pago pago(Long compromisoId, boolean aTiempo) {
        return Pago.builder().id(compromisoId).numeroComprobante("CPR-00000" + compromisoId).compromisoId(compromisoId)
                .usuarioId(1L).fechaHora(LocalDateTime.now()).canal(Canal.APP).aTiempo(aTiempo).build();
    }
}
