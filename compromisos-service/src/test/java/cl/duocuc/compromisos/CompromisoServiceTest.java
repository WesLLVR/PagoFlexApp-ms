package cl.duocuc.compromisos;

import cl.duocuc.compromisos.client.UsuarioClient;
import cl.duocuc.compromisos.dto.AnularRequest;
import cl.duocuc.compromisos.dto.CompromisoResponse;
import cl.duocuc.compromisos.dto.CrearPersonaDto;
import cl.duocuc.compromisos.dto.PagableResponse;
import cl.duocuc.compromisos.dto.PaginaResponse;
import cl.duocuc.compromisos.dto.RegistroCompromisoRequest;
import cl.duocuc.compromisos.dto.ResumenCobranzaResponse;
import cl.duocuc.compromisos.dto.ResumenMesResponse;
import cl.duocuc.compromisos.dto.UsuarioDto;
import cl.duocuc.compromisos.model.Compromiso;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.model.EstadoCompromiso;
import cl.duocuc.compromisos.model.MotivoAnulacion;
import cl.duocuc.compromisos.repository.CompromisoRepository;
import cl.duocuc.compromisos.service.CompromisoService;
import cl.duocuc.compromisos.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompromisoServiceTest {

    @Mock
    private CompromisoRepository compromisoRepository;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private CompromisoService compromisoService;

    private final Empresa club = Empresa.builder().id(6L).nombre("Club Los Halcones").razonSocial("Club Deportivo")
            .recargo(2000).build();

    private Compromiso cuota(Long id, int numero, LocalDate vencimiento, EstadoCompromiso estado) {
        return Compromiso.builder()
                .id(id).folio("CP-" + id).usuarioId(1L).nombreUsuario("Camila Rojas").rutUsuario("15.834.207-3")
                .empresa(club).deuda("Cuota social").concepto("Cuota social").numeroCuota(numero).totalCuotas(12)
                .monto(15000).vencimiento(vencimiento).estado(estado).fechaRegistro(LocalDate.now())
                .build();
    }

    @Test
    void pagable_conUnaCuotaAnteriorPendiente_quedaBloqueado() {
        Compromiso cuota10 = cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PENDIENTE);
        Compromiso cuota11 = cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.PENDIENTE);
        when(compromisoRepository.findById(11L)).thenReturn(Optional.of(cuota11));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(1L, 6L, "Cuota social"))
                .thenReturn(List.of(cuota10, cuota11));

        PagableResponse respuesta = compromisoService.pagable(11L);

        assertFalse(respuesta.pagable());
        assertTrue(respuesta.motivo().startsWith("Primero paga la cuota 10/12"));
    }

    @Test
    void obtener_cuotaPendienteYaVencida_sumaElRecargo() {
        when(compromisoRepository.findById(10L))
                .thenReturn(Optional.of(cuota(10L, 10, LocalDate.now().minusDays(3), EstadoCompromiso.PENDIENTE)));

        CompromisoResponse respuesta = compromisoService.obtener(10L);

        assertEquals("VENCIDO", respuesta.estado());
        assertEquals(17000, respuesta.totalAPagar());
        assertEquals(3, respuesta.diasAtraso());
    }

    @Test
    void cambiarEstado_aPagadoDesdeEnRevision_responde409() {
        when(compromisoRepository.findById(10L))
                .thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.EN_REVISION)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> compromisoService.cambiarEstado(10L, "PAGADO"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(compromisoRepository, never()).save(any(Compromiso.class));
    }

    @Test
    void listar_conFiltroVencido_devuelveSoloLosVencidos() {
        when(compromisoRepository.findByUsuarioId(1L)).thenReturn(List.of(
                cuota(9L, 9, LocalDate.now().minusDays(30), EstadoCompromiso.PAGADO),
                cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PENDIENTE),
                cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.PENDIENTE)));

        PaginaResponse<CompromisoResponse> pagina = compromisoService.listar(1L, null, "VENCIDO", null, null, 0, 5);

        assertEquals(1, pagina.totalElementos());
        assertEquals(10L, pagina.contenido().get(0).id());
    }

    @Test
    void registrar_compromisoRepetido_responde409() {
        when(empresaService.buscar(6L)).thenReturn(club);
        when(usuarioClient.buscarPorRut("15.834.207-3"))
                .thenReturn(new UsuarioDto(1L, "Camila Rojas", "15.834.207-3", "camila@pagoflex.cl"));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndConceptoIgnoreCase(1L, 6L, "Cuota social"))
                .thenReturn(List.of(cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.PENDIENTE)));

        RegistroCompromisoRequest datos = new RegistroCompromisoRequest(6L, 18L, "15.834.207-3", null, null, null,
                "Cuota social", 11, 12, 15000, LocalDate.now().plusDays(10));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> compromisoService.registrar(datos));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void registrar_personaNuevaSinDatosDeContacto_responde400() {
        when(empresaService.buscar(6L)).thenReturn(club);
        when(usuarioClient.buscarPorRut("11.111.111-1")).thenReturn(null);

        RegistroCompromisoRequest datos = new RegistroCompromisoRequest(6L, 18L, "11.111.111-1", null, null, null,
                "Cuota social", 1, 12, 15000, LocalDate.now().plusDays(10));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> compromisoService.registrar(datos));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(usuarioClient, never()).crear(any());
    }

    private void devolverAlGuardar() {
        when(compromisoRepository.save(any(Compromiso.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private RegistroCompromisoRequest registro(String rut, String concepto, Integer cuota, Integer total, Integer monto,
                                               LocalDate vencimiento) {
        return new RegistroCompromisoRequest(6L, 18L, rut, null, null, null, concepto, cuota, total, monto, vencimiento);
    }

    @Test
    void listar_porEmpresaConMesYBusqueda_filtraYOrdena() {
        LocalDate enDiezDias = LocalDate.now().plusDays(10);
        Compromiso otraPersona = cuota(20L, 3, enDiezDias, EstadoCompromiso.PENDIENTE);
        otraPersona.setNombreUsuario("Luis Muñoz");
        when(compromisoRepository.findByEmpresaId(6L)).thenReturn(List.of(
                cuota(11L, 11, enDiezDias, EstadoCompromiso.PENDIENTE), otraPersona,
                cuota(12L, 12, enDiezDias.plusMonths(1), EstadoCompromiso.PENDIENTE)));

        PaginaResponse<CompromisoResponse> pagina = compromisoService.listar(null, 6L, "pendiente", "camila",
                YearMonth.from(enDiezDias).toString(), 0, 5);

        assertEquals(1, pagina.totalElementos());
        assertEquals(11L, pagina.contenido().get(0).id());
    }

    @Test
    void listar_vigentesYTodos_usanLaBaseCompleta() {
        when(compromisoRepository.findAll()).thenReturn(List.of(
                cuota(9L, 9, LocalDate.now().minusDays(30), EstadoCompromiso.PAGADO),
                cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PENDIENTE),
                cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.ANULADO)));

        PaginaResponse<CompromisoResponse> vigentes = compromisoService.listar(null, null, "VIGENTE", "", null, 0, 5);
        PaginaResponse<CompromisoResponse> todos = compromisoService.listar(null, null, null, null, "", 0, 5);

        assertEquals(1, vigentes.totalElementos());
        assertEquals(3, todos.totalElementos());
        assertEquals(11L, todos.contenido().get(0).id());
    }

    @Test
    void obtener_noExiste_responde404() {
        when(compromisoRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> compromisoService.obtener(99L)).getStatusCode());
    }

    @Test
    void cuotas_devuelveLaDeudaOrdenadaPorVencimiento() {
        Compromiso cuota10 = cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PAGADO);
        Compromiso cuota11 = cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.PENDIENTE);
        when(compromisoRepository.findById(11L)).thenReturn(Optional.of(cuota11));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(1L, 6L, "Cuota social"))
                .thenReturn(List.of(cuota11, cuota10));

        List<CompromisoResponse> cuotas = compromisoService.cuotas(11L);

        assertEquals(List.of(10L, 11L), cuotas.stream().map(CompromisoResponse::id).toList());
    }

    @Test
    void pagable_segunElEstado() {
        Compromiso sinCuotas = cuota(13L, 1, LocalDate.now().plusDays(5), EstadoCompromiso.PENDIENTE);
        Compromiso anteriorSinCuotas = cuota(12L, 1, LocalDate.now().minusDays(5), EstadoCompromiso.PENDIENTE);
        anteriorSinCuotas.setNumeroCuota(null);
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PAGADO)));
        when(compromisoRepository.findById(11L)).thenReturn(Optional.of(cuota(11L, 11, LocalDate.now(), EstadoCompromiso.ANULADO)));
        when(compromisoRepository.findById(12L)).thenReturn(Optional.of(cuota(12L, 12, LocalDate.now(), EstadoCompromiso.EN_REVISION)));
        when(compromisoRepository.findById(13L)).thenReturn(Optional.of(sinCuotas));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(1L, 6L, "Cuota social"))
                .thenReturn(List.of(anteriorSinCuotas, sinCuotas));

        assertEquals("Este compromiso ya está pagado.", compromisoService.pagable(10L).motivo());
        assertTrue(compromisoService.pagable(11L).motivo().contains("anuló"));
        assertTrue(compromisoService.pagable(12L).motivo().contains("revisando"));
        assertEquals("Primero paga \"Cuota social\" de Club Los Halcones, que vence el "
                + anteriorSinCuotas.getVencimiento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".",
                compromisoService.pagable(13L).motivo());
    }

    @Test
    void pagable_sinCuotasAnteriores_sePuedePagar() {
        Compromiso cuota11 = cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.PENDIENTE);
        when(compromisoRepository.findById(11L)).thenReturn(Optional.of(cuota11));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(1L, 6L, "Cuota social"))
                .thenReturn(List.of(cuota11));

        PagableResponse respuesta = compromisoService.pagable(11L);

        assertTrue(respuesta.pagable());
        assertNull(respuesta.motivo());
    }

    @Test
    void resumenMes_sumaLoPendienteDelMesConRecargo() {
        LocalDate finDeMes = YearMonth.now().atEndOfMonth();
        when(compromisoRepository.findByUsuarioId(1L)).thenReturn(List.of(
                cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PENDIENTE),
                cuota(11L, 11, finDeMes, EstadoCompromiso.PENDIENTE),
                cuota(12L, 12, finDeMes.plusDays(1), EstadoCompromiso.PENDIENTE),
                cuota(9L, 9, LocalDate.now().minusDays(30), EstadoCompromiso.PAGADO)));

        ResumenMesResponse resumen = compromisoService.resumenMes(1L);

        assertEquals(15000 + 2000 + 15000, resumen.totalMes());
        assertEquals(finDeMes, resumen.proximoVencimiento());
        assertEquals("Club Los Halcones", resumen.empresaProximo());
        assertEquals(1, resumen.cantidadVencidos());
    }

    @Test
    void resumenMes_sinCompromisos_noTieneProximo() {
        when(compromisoRepository.findByUsuarioId(2L)).thenReturn(List.of());

        ResumenMesResponse resumen = compromisoService.resumenMes(2L);

        assertEquals(0, resumen.totalMes());
        assertNull(resumen.proximoVencimiento());
        assertNull(resumen.empresaProximo());
    }

    @Test
    void proximosYConsiderados_filtranPorFechaYEstado() {
        when(compromisoRepository.findByUsuarioId(1L)).thenReturn(List.of(
                cuota(8L, 8, LocalDate.now().minusMonths(13), EstadoCompromiso.PAGADO),
                cuota(9L, 9, LocalDate.now().minusDays(40), EstadoCompromiso.PAGADO),
                cuota(10L, 10, LocalDate.now().minusDays(1), EstadoCompromiso.PENDIENTE),
                cuota(11L, 11, LocalDate.now().plusDays(2), EstadoCompromiso.PENDIENTE),
                cuota(12L, 12, LocalDate.now().plusDays(20), EstadoCompromiso.PENDIENTE),
                cuota(13L, 7, LocalDate.now().minusDays(70), EstadoCompromiso.ANULADO)));

        List<CompromisoResponse> proximos = compromisoService.proximos(1L, 3);
        List<CompromisoResponse> considerados = compromisoService.consideradosCumplimiento(1L);

        assertEquals(List.of(10L, 11L), proximos.stream().map(CompromisoResponse::id).toList());
        assertEquals(List.of(9L, 10L), considerados.stream().map(CompromisoResponse::id).toList());
    }

    @Test
    void resumenCobranza_calculaTasaYRecaudado() {
        LocalDate esteMes = LocalDate.now().withDayOfMonth(1);
        when(compromisoRepository.findByEmpresaId(6L)).thenReturn(List.of(
                cuota(9L, 9, esteMes, EstadoCompromiso.PAGADO),
                cuota(10L, 10, esteMes, EstadoCompromiso.EN_REVISION),
                cuota(11L, 11, esteMes, EstadoCompromiso.ANULADO),
                cuota(12L, 12, esteMes.minusMonths(1), EstadoCompromiso.PAGADO)));

        ResumenCobranzaResponse resumen = compromisoService.resumenCobranza(6L, null);
        ResumenCobranzaResponse vacio = compromisoService.resumenCobranza(6L, "2020-01");

        assertEquals(1, resumen.pagados());
        assertEquals(1, resumen.enRevision());
        assertEquals(1, resumen.anulados());
        assertEquals(15000L, resumen.montoRecaudado());
        assertEquals(50.0, resumen.tasa());
        assertNull(vacio.tasa());
    }

    @Test
    void registrar_personaExistente_guardaConFolioNuevo() {
        when(empresaService.buscar(6L)).thenReturn(club);
        when(usuarioClient.buscarPorRut("15.834.207-3"))
                .thenReturn(new UsuarioDto(1L, "Camila Rojas", "15.834.207-3", "camila@pagoflex.cl"));
        when(compromisoRepository.findByUsuarioIdAndEmpresaIdAndConceptoIgnoreCase(1L, 6L, "Cuota social"))
                .thenReturn(List.of(cuota(11L, 11, LocalDate.now().plusDays(29), EstadoCompromiso.ANULADO)));
        when(compromisoRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(cuota(51L, 1, LocalDate.now(), EstadoCompromiso.PAGADO)));
        devolverAlGuardar();

        CompromisoResponse respuesta = compromisoService.registrar(
                registro("15.834.207-3", " Cuota social ", 11, 12, 15000, LocalDate.now().plusDays(10)));

        assertEquals("CP-" + LocalDate.now().getYear() + "-0352", respuesta.folio());
        assertEquals("PENDIENTE", respuesta.estado());
    }

    @Test
    void registrar_personaNueva_laCreaEnUsuarios() {
        when(empresaService.buscar(6L)).thenReturn(club);
        when(usuarioClient.buscarPorRut("20.111.222-3")).thenReturn(null);
        when(usuarioClient.crear(any(CrearPersonaDto.class)))
                .thenReturn(new UsuarioDto(21L, "Ana Torres", "20.111.222-3", "ana@pagoflex.cl"));
        when(compromisoRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        devolverAlGuardar();

        CompromisoResponse respuesta = compromisoService.registrar(new RegistroCompromisoRequest(6L, 18L, "20.111.222-3",
                "Ana Torres", "ana@pagoflex.cl", "912345678", "Cuota social", null, null, 15000, LocalDate.now()));

        assertEquals(21L, respuesta.usuarioId());
        assertEquals("CP-" + LocalDate.now().getYear() + "-0301", respuesta.folio());
    }

    @Test
    void registrar_conDatosInvalidos_responde400() {
        when(empresaService.buscar(6L)).thenReturn(club);
        LocalDate manana = LocalDate.now().plusDays(1);
        List<RegistroCompromisoRequest> invalidos = List.of(
                registro(" ", "Cuota social", 1, 12, 15000, manana),
                registro("15.834.207-3", "Cu", 1, 12, 15000, manana),
                registro("15.834.207-3", "Cuota social", 1, 12, 0, manana),
                registro("15.834.207-3", "Cuota social", 1, 12, null, manana),
                registro("15.834.207-3", "Cuota social", 1, 12, 15000, LocalDate.now().minusDays(1)),
                registro("15.834.207-3", "Cuota social", 1, null, 15000, manana),
                registro("15.834.207-3", "Cuota social", 13, 12, 15000, manana));

        for (RegistroCompromisoRequest datos : invalidos) {
            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ResponseStatusException.class, () -> compromisoService.registrar(datos)).getStatusCode());
        }
        verify(compromisoRepository, never()).save(any(Compromiso.class));
    }

    @Test
    void cambiarEstado_transicionesPermitidas() {
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PENDIENTE)));
        when(compromisoRepository.findById(11L)).thenReturn(Optional.of(cuota(11L, 11, LocalDate.now(), EstadoCompromiso.EN_REVISION)));
        devolverAlGuardar();

        assertEquals("PAGADO", compromisoService.cambiarEstado(10L, "pagado").estado());
        assertEquals("PENDIENTE", compromisoService.cambiarEstado(11L, "PENDIENTE").estado());
        verify(compromisoRepository, times(2)).save(any(Compromiso.class));
    }

    @Test
    void cambiarEstado_estadoInvalidoOAnulado_falla() {
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PENDIENTE)));

        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> compromisoService.cambiarEstado(10L, "BORRADO")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> compromisoService.cambiarEstado(10L, "ANULADO")).getStatusCode());
    }

    @Test
    void anular_guardaElMotivo() {
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PENDIENTE)));
        devolverAlGuardar();

        CompromisoResponse respuesta = compromisoService.anular(10L, new AnularRequest("repactacion", null, 18L));

        assertEquals("ANULADO", respuesta.estado());
        assertEquals(MotivoAnulacion.REPACTACION.getEtiqueta(), respuesta.motivoAnulacion());
    }

    @Test
    void anular_conErrores_falla() {
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PENDIENTE)));
        when(compromisoRepository.findById(9L)).thenReturn(Optional.of(cuota(9L, 9, LocalDate.now(), EstadoCompromiso.PAGADO)));

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> compromisoService.anular(10L, new AnularRequest(" ", "", 18L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> compromisoService.anular(10L, new AnularRequest("INVENTADO", "", 18L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> compromisoService.anular(10L, new AnularRequest("OTRO", "corto", 18L))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> compromisoService.anular(9L, new AnularRequest("ERROR_REGISTRO", "", 18L))).getStatusCode());
        verify(compromisoRepository, never()).save(any(Compromiso.class));
    }

    @Test
    void eliminar_soloPendientes() {
        Compromiso pendiente = cuota(10L, 10, LocalDate.now(), EstadoCompromiso.PENDIENTE);
        when(compromisoRepository.findById(10L)).thenReturn(Optional.of(pendiente));
        when(compromisoRepository.findById(9L)).thenReturn(Optional.of(cuota(9L, 9, LocalDate.now(), EstadoCompromiso.PAGADO)));

        compromisoService.eliminar(10L);

        verify(compromisoRepository).delete(pendiente);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> compromisoService.eliminar(9L)).getStatusCode());
    }
}
