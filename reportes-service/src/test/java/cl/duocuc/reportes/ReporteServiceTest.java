package cl.duocuc.reportes;

import cl.duocuc.reportes.client.CompromisoClient;
import cl.duocuc.reportes.client.UsuarioClient;
import cl.duocuc.reportes.dto.CompromisoDto;
import cl.duocuc.reportes.dto.CrearReporteRequest;
import cl.duocuc.reportes.dto.PaginaResponse;
import cl.duocuc.reportes.dto.ReporteResponse;
import cl.duocuc.reportes.dto.ResumenReportesResponse;
import cl.duocuc.reportes.dto.UsuarioDto;
import cl.duocuc.reportes.model.EstadoReporte;
import cl.duocuc.reportes.model.HistorialEstado;
import cl.duocuc.reportes.model.MotivoReporte;
import cl.duocuc.reportes.model.Reporte;
import cl.duocuc.reportes.repository.HistorialEstadoRepository;
import cl.duocuc.reportes.repository.ReporteRepository;
import cl.duocuc.reportes.service.AlmacenamientoService;
import cl.duocuc.reportes.service.ReporteService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private CompromisoClient compromisoClient;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @InjectMocks
    private ReporteService reporteService;

    @BeforeEach
    void prepararPeticion() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiarPeticion() {
        RequestContextHolder.resetRequestAttributes();
    }

    private Reporte reporte(EstadoReporte estado, Long agenteId) {
        return Reporte.builder()
                .id(12L).numero("REP-0012").compromisoId(36L).folio("CP-2026-0106").concepto("Cuota pie")
                .empresa("Altos del Maipo").usuarioId(2L).nombreUsuario("Luis Muñoz").motivo(MotivoReporte.OTRO)
                .comentario("No sé por qué me cobran esto").estado(estado).agenteId(agenteId)
                .nombreAgente(agenteId == null ? null : "Andrés Lagos").fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void crear_sinMotivo_responde400() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 2L, null, "Comentario")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(compromisoClient, never()).obtener(any());
    }

    @Test
    void crear_compromisoDeOtraPersona_responde403() {
        when(compromisoClient.obtener(36L)).thenReturn(
                new CompromisoDto(36L, "CP-2026-0106", 2L, "Luis Muñoz", "Altos del Maipo", "Cuota pie", "PENDIENTE"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 1L, "YA_PAGUE", "")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void tomar_reporteAbierto_pasaAEnGestionConElAgente() {
        Reporte abierto = reporte(EstadoReporte.ABIERTO, null);
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(abierto));
        when(usuarioClient.obtener(9L)).thenReturn(new UsuarioDto(9L, "Andrés Lagos", "AGENTE"));

        ReporteResponse respuesta = reporteService.tomar(12L, 9L);

        assertEquals("EN_GESTION", respuesta.estado());
        assertEquals("Andrés Lagos", respuesta.nombreAgente());
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void resolver_sinRespuesta_responde400() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.EN_GESTION, 9L)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> reporteService.resolver(12L, 9L, "  "));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void cerrar_reporteQueSigueAbierto_responde409() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.ABIERTO, null)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> reporteService.cerrar(12L, 2L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(compromisoClient, never()).cambiarEstado(any(), any());
    }

    @Test
    void cerrar_reporteResuelto_liberaElCompromiso() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.RESUELTO, 9L)));

        ReporteResponse respuesta = reporteService.cerrar(12L, 2L);

        assertEquals("CERRADO", respuesta.estado());
        verify(compromisoClient).cambiarEstado(36L, "PENDIENTE");
    }

    private CompromisoDto compromisoDeLuis(String estado) {
        return new CompromisoDto(36L, "CP-2026-0106", 2L, "Luis Muñoz", "Altos del Maipo", "Cuota pie", estado);
    }

    private void devolverAlGuardar() {
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void crear_reporteValido_quedaAbiertoYElCompromisoEnRevision() {
        when(compromisoClient.obtener(36L)).thenReturn(compromisoDeLuis("VENCIDO"));
        when(reporteRepository.findAll()).thenReturn(List.of(reporte(EstadoReporte.CERRADO, 9L)));
        devolverAlGuardar();

        ReporteResponse respuesta = reporteService.crear(new CrearReporteRequest(36L, 2L, "ya_pague", " Pagué por transferencia "));

        assertEquals("REP-0013", respuesta.numero());
        assertEquals("ABIERTO", respuesta.estado());
        assertEquals("Pagué por transferencia", respuesta.comentario());
        verify(compromisoClient).cambiarEstado(36L, "EN_REVISION");
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void crear_siCompromisosFalla_borraElReporte() {
        when(compromisoClient.obtener(36L)).thenReturn(compromisoDeLuis("PENDIENTE"));
        devolverAlGuardar();
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "No se puede")).when(compromisoClient).cambiarEstado(36L, "EN_REVISION");

        assertThrows(ResponseStatusException.class, () -> reporteService.crear(new CrearReporteRequest(36L, 2L, "NO_RECONOZCO", "")));

        verify(reporteRepository).delete(any(Reporte.class));
    }

    @Test
    void crear_conDatosInvalidos_falla() {
        when(compromisoClient.obtener(36L)).thenReturn(compromisoDeLuis("PAGADO"));

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 2L, "INVENTADO", ""))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 2L, "OTRO", "corto"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 2L, "OTRO", "x".repeat(301)))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> reporteService.crear(new CrearReporteRequest(36L, 2L, "YA_PAGUE", null))).getStatusCode());
    }

    @Test
    void listar_filtraYPoneLosAtrasadosPrimero() {
        Reporte atrasado = reporte(EstadoReporte.ABIERTO, null);
        atrasado.setId(11L);
        atrasado.setFechaCreacion(LocalDateTime.now().minusDays(10));
        Reporte nuevo = reporte(EstadoReporte.EN_GESTION, 9L);
        Reporte cerrado = reporte(EstadoReporte.CERRADO, 9L);
        cerrado.setId(13L);
        when(reporteRepository.findAll()).thenReturn(List.of(nuevo, cerrado, atrasado));
        when(reporteRepository.findByUsuarioId(2L)).thenReturn(List.of(nuevo));

        PaginaResponse<ReporteResponse> todos = reporteService.listar(null, null, null, null, false, 0, 5);
        PaginaResponse<ReporteResponse> soloAtrasados = reporteService.listar("", null, null, "", true, 0, 5);
        PaginaResponse<ReporteResponse> delAgente = reporteService.listar("en_gestion", null, 9L, "altos", false, 0, 5);
        PaginaResponse<ReporteResponse> delUsuario = reporteService.listar(null, 2L, null, "REP-0012", false, 0, 5);

        assertEquals(11L, todos.contenido().get(0).id());
        assertTrue(todos.contenido().get(0).atrasado());
        assertEquals(1, soloAtrasados.totalElementos());
        assertEquals(12L, delAgente.contenido().get(0).id());
        assertEquals(1, delUsuario.totalElementos());
    }

    @Test
    void resumen_cuentaPorEstado() {
        Reporte atrasado = reporte(EstadoReporte.ABIERTO, null);
        atrasado.setFechaCreacion(LocalDateTime.now().minusDays(10));
        when(reporteRepository.findAll()).thenReturn(List.of(atrasado, reporte(EstadoReporte.EN_GESTION, 9L),
                reporte(EstadoReporte.RESUELTO, 9L), reporte(EstadoReporte.CERRADO, 9L)));

        ResumenReportesResponse resumen = reporteService.resumen();

        assertEquals(1, resumen.abiertos());
        assertEquals(1, resumen.enGestion());
        assertEquals(1, resumen.resueltos());
        assertEquals(1, resumen.cerrados());
        assertEquals(1, resumen.atrasados());
    }

    @Test
    void obtenerYUltimo_incluyenElHistorial() {
        Reporte abierto = reporte(EstadoReporte.EN_GESTION, 9L);
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(abierto));
        when(reporteRepository.findTopByCompromisoIdOrderByIdDesc(36L)).thenReturn(Optional.of(abierto));
        when(reporteRepository.findTopByCompromisoIdOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(historialRepository.findByReporteIdOrderByFechaAsc(12L)).thenReturn(List.of(
                HistorialEstado.builder().reporteId(12L).estadoNuevo(EstadoReporte.ABIERTO).autor("Luis Muñoz").fecha(LocalDateTime.now()).build(),
                HistorialEstado.builder().reporteId(12L).estadoAnterior(EstadoReporte.ABIERTO).estadoNuevo(EstadoReporte.EN_GESTION)
                        .autor("Andrés Lagos").fecha(LocalDateTime.now()).build()));

        ReporteResponse respuesta = reporteService.obtener(12L);

        assertEquals(2, respuesta.historial().size());
        assertNull(respuesta.historial().get(0).estadoAnterior());
        assertEquals("En gestión", respuesta.historial().get(1).estadoNombre());
        assertEquals("REP-0012", reporteService.ultimoDeCompromiso(36L).numero());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> reporteService.ultimoDeCompromiso(99L)).getStatusCode());
    }

    @Test
    void tomar_sinSerAgente_responde403() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.ABIERTO, null)));
        when(usuarioClient.obtener(1L)).thenReturn(new UsuarioDto(1L, "Camila Rojas", "USUARIO_FINAL"));

        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class, () -> reporteService.tomar(12L, 1L)).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class, () -> reporteService.tomar(12L, null)).getStatusCode());
    }

    @Test
    void reporte_inexistente_responde404() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, () -> reporteService.obtener(99L)).getStatusCode());
    }

    @Test
    void resolver_conRespuestaValida_quedaResuelto() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.EN_GESTION, 9L)));

        ReporteResponse respuesta = reporteService.resolver(12L, 9L, " Revisamos tu pago y quedó registrado ");

        assertEquals("RESUELTO", respuesta.estado());
        assertEquals("Revisamos tu pago y quedó registrado", respuesta.respuesta());
    }

    @Test
    void resolver_otroAgenteORespuestaCorta_falla() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.EN_GESTION, 9L)));

        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> reporteService.resolver(12L, 10L, "Respuesta de otro agente")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.resolver(12L, 9L, "Muy corta")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.resolver(12L, 9L, null)).getStatusCode());
    }

    @Test
    void reabrir_reporteResuelto_vuelveAEnGestion() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.RESUELTO, 9L)));

        ReporteResponse respuesta = reporteService.reabrir(12L, 2L, "Sigue apareciendo como pendiente");

        assertEquals("EN_GESTION", respuesta.estado());
        assertNull(respuesta.fechaResolucion());
    }

    @Test
    void reabrir_deOtroUsuarioOSinComentario_falla() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.RESUELTO, 9L)));

        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> reporteService.reabrir(12L, 1L, "Sigue apareciendo como pendiente")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> reporteService.reabrir(12L, 2L, null)).getStatusCode());
    }

    @Test
    void cerrar_ignoraElConflictoPeroNoOtrosErrores() {
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(reporte(EstadoReporte.RESUELTO, 9L)));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya estaba pendiente")).when(compromisoClient).cambiarEstado(36L, "PENDIENTE");

        assertEquals("CERRADO", reporteService.cerrar(12L, 2L).estado());

        Reporte otro = reporte(EstadoReporte.RESUELTO, 9L);
        when(reporteRepository.findById(13L)).thenReturn(Optional.of(otro));
        otro.setId(13L);
        otro.setCompromisoId(37L);
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Caído")).when(compromisoClient).cambiarEstado(37L, "PENDIENTE");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> reporteService.cerrar(13L, 2L)).getStatusCode());
    }

    @Test
    void foto_subirVerYQuitar() {
        Reporte conFoto = reporte(EstadoReporte.ABIERTO, null);
        conFoto.setFoto("vieja.jpg");
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[]{1});
        when(reporteRepository.findById(12L)).thenReturn(Optional.of(conFoto));
        when(almacenamientoService.guardar(foto)).thenReturn("nueva.jpg");
        when(almacenamientoService.leer("nueva.jpg")).thenReturn(new byte[]{9});

        assertTrue(reporteService.subirFoto(12L, foto).fotoUrl().endsWith("/api/reportes/12/foto?v=nueva.jpg"));
        verify(almacenamientoService).eliminar("vieja.jpg");
        assertArrayEquals(new byte[]{9}, reporteService.obtenerFoto(12L));
        assertNull(reporteService.eliminarFoto(12L).fotoUrl());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, () -> reporteService.obtenerFoto(12L)).getStatusCode());
    }

    @Test
    void atrasado_soloCuentaReportesAbiertosOEnGestion() {
        Reporte resueltoAntiguo = reporte(EstadoReporte.RESUELTO, 9L);
        resueltoAntiguo.setFechaCreacion(LocalDateTime.now().minusDays(20));

        assertFalse(reporteService.atrasado(resueltoAntiguo));
        assertFalse(reporteService.atrasado(reporte(EstadoReporte.ABIERTO, null)));
    }
}
