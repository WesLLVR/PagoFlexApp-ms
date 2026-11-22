package cl.duocuc.compromisos;

import cl.duocuc.compromisos.controller.CompromisoController;
import cl.duocuc.compromisos.dto.AnularRequest;
import cl.duocuc.compromisos.dto.CompromisoResponse;
import cl.duocuc.compromisos.dto.PagableResponse;
import cl.duocuc.compromisos.dto.PaginaResponse;
import cl.duocuc.compromisos.dto.RegistroCompromisoRequest;
import cl.duocuc.compromisos.dto.ResumenCobranzaResponse;
import cl.duocuc.compromisos.dto.ResumenMesResponse;
import cl.duocuc.compromisos.service.CompromisoService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompromisoControllerTest {

    @Mock
    private CompromisoService compromisoService;

    @InjectMocks
    private CompromisoController compromisoController;

    private MockMvc mockMvc;

    private final CompromisoResponse compromiso = new CompromisoResponse(10L, "CP-2026-0102", 1L, "Camila Rojas",
            "15.834.207-3", 6L, "Club Los Halcones", "Club Deportivo", "Cuota social", "Cuota social", 10, 12,
            15000, 2000, 17000, LocalDate.now().minusDays(1), "VENCIDO", 1, null, null);

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(compromisoController).build();
    }

    @Test
    void obtener_devuelve200ConElEstadoCalculado() throws Exception {
        when(compromisoService.obtener(10L)).thenReturn(compromiso);

        mockMvc.perform(get("/api/compromisos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VENCIDO"))
                .andExpect(jsonPath("$.totalAPagar").value(17000));
    }

    @Test
    void listar_pasaLosFiltrosYUsaTamanoCinco() throws Exception {
        when(compromisoService.listar(1L, null, "VIGENTE", "club", null, 0, 5))
                .thenReturn(new PaginaResponse<>(List.of(compromiso), 0, 1, 1));

        mockMvc.perform(get("/api/compromisos").param("usuarioId", "1").param("estado", "VIGENTE").param("buscar", "club"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].folio").value("CP-2026-0102"));
    }

    @Test
    void consultasDelUsuario_respondenOk() throws Exception {
        when(compromisoService.cuotas(10L)).thenReturn(List.of(compromiso));
        when(compromisoService.pagable(10L)).thenReturn(new PagableResponse(true, null));
        when(compromisoService.resumenMes(1L)).thenReturn(new ResumenMesResponse(32000, LocalDate.now(), "Club Los Halcones", 1));
        when(compromisoService.proximos(1L, 3)).thenReturn(List.of(compromiso));
        when(compromisoService.consideradosCumplimiento(1L)).thenReturn(List.of(compromiso));

        mockMvc.perform(get("/api/compromisos/10/cuotas")).andExpect(jsonPath("$[0].id").value(10));
        mockMvc.perform(get("/api/compromisos/10/pagable")).andExpect(jsonPath("$.pagable").value(true));
        mockMvc.perform(get("/api/compromisos/resumen-mes").param("usuarioId", "1")).andExpect(jsonPath("$.totalMes").value(32000));
        mockMvc.perform(get("/api/compromisos/proximos").param("usuarioId", "1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/compromisos/considerados").param("usuarioId", "1")).andExpect(status().isOk());
    }

    @Test
    void resumenCobranza_devuelveLaTasa() throws Exception {
        when(compromisoService.resumenCobranza(6L, "2026-11"))
                .thenReturn(new ResumenCobranzaResponse("2026-11", 3, 1, 0, 0, 0, 45000L, 75.0));

        mockMvc.perform(get("/api/compromisos/resumen-cobranza").param("empresaId", "6").param("mes", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasa").value(75.0));
    }

    @Test
    void registrar_devuelve201() throws Exception {
        when(compromisoService.registrar(any(RegistroCompromisoRequest.class))).thenReturn(compromiso);

        mockMvc.perform(post("/api/compromisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresaId\":6,\"ejecutivoId\":18,\"rut\":\"15.834.207-3\",\"concepto\":\"Cuota social\",\"monto\":15000,\"vencimiento\":\"2030-01-10\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void cambiarEstado_noPermitido_devuelve409() throws Exception {
        when(compromisoService.cambiarEstado(10L, "PAGADO"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "No se puede pasar el compromiso"));

        mockMvc.perform(patch("/api/compromisos/10/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"PAGADO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void anularYEliminar_respondenCorrectamente() throws Exception {
        when(compromisoService.anular(eq(10L), any(AnularRequest.class))).thenReturn(compromiso);

        mockMvc.perform(patch("/api/compromisos/10/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"REPACTACION\",\"comentario\":\"\",\"ejecutivoId\":18}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/compromisos/10")).andExpect(status().isNoContent());

        verify(compromisoService).eliminar(10L);
    }
}
