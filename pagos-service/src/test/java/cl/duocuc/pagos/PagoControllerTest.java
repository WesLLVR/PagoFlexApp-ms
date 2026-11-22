package cl.duocuc.pagos;

import cl.duocuc.pagos.controller.PagoController;
import cl.duocuc.pagos.dto.CumplimientoResponse;
import cl.duocuc.pagos.dto.PaginaResponse;
import cl.duocuc.pagos.dto.PagoRealizadoResponse;
import cl.duocuc.pagos.dto.PagoRequest;
import cl.duocuc.pagos.dto.PagoResponse;
import cl.duocuc.pagos.service.PagoService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PagoController pagoController;

    private MockMvc mockMvc;

    private final PagoResponse pago = new PagoResponse(1L, "CPR-000454", 10L, 1L, "Camila Rojas", "15.834.207-3",
            "CP-2026-0102", "Club Los Halcones", "Club Deportivo", "Cuota social", 10, 12, 15000, LocalDate.now(),
            LocalDateTime.now(), 15000, "APP", "App", "Transferencia", true);

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(pagoController).build();
    }

    @Test
    void pagar_devuelve201ConElComprobante() throws Exception {
        when(pagoService.pagar(new PagoRequest(10L, "Transferencia"))).thenReturn(new PagoRealizadoResponse(pago, null));

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compromisoId\":10,\"medioPago\":\"Transferencia\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pago.numeroComprobante").value("CPR-000454"));
    }

    @Test
    void pagar_yaPagado_devuelve409() throws Exception {
        when(pagoService.pagar(new PagoRequest(10L, "Transferencia")))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Este compromiso ya está pagado."));

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compromisoId\":10,\"medioPago\":\"Transferencia\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listar_conFechas_devuelveLaPagina() throws Exception {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 11, 30);
        when(pagoService.listar(1L, null, null, desde, hasta, null, 0, 5))
                .thenReturn(new PaginaResponse<>(List.of(pago), 0, 1, 1));

        mockMvc.perform(get("/api/pagos").param("usuarioId", "1").param("desde", "2026-10-01").param("hasta", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].empresa").value("Club Los Halcones"));
    }

    @Test
    void consultas_respondenOk() throws Exception {
        when(pagoService.empresas(1L)).thenReturn(List.of("Club Los Halcones"));
        when(pagoService.cumplimiento(1L)).thenReturn(new CumplimientoResponse(10, 9, 90.0, "AL_DIA", "Al día", "Vas muy bien"));
        when(pagoService.porCompromiso(10L)).thenReturn(pago);
        when(pagoService.obtener(1L)).thenReturn(pago);

        mockMvc.perform(get("/api/pagos/empresas").param("usuarioId", "1")).andExpect(jsonPath("$[0]").value("Club Los Halcones"));
        mockMvc.perform(get("/api/pagos/cumplimiento").param("usuarioId", "1")).andExpect(jsonPath("$.categoria").value("AL_DIA"));
        mockMvc.perform(get("/api/pagos/compromiso/10")).andExpect(jsonPath("$.compromisoId").value(10));
        mockMvc.perform(get("/api/pagos/1")).andExpect(status().isOk());
    }
}
