package cl.duocuc.reportes;

import cl.duocuc.reportes.controller.ReporteController;
import cl.duocuc.reportes.dto.CrearReporteRequest;
import cl.duocuc.reportes.dto.PaginaResponse;
import cl.duocuc.reportes.dto.ReporteResponse;
import cl.duocuc.reportes.dto.ResumenReportesResponse;
import cl.duocuc.reportes.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    private MockMvc mockMvc;

    private ReporteResponse reporte(String estado) {
        return new ReporteResponse(12L, "REP-0012", 36L, "CP-2026-0106", "Cuota pie", "Altos del Maipo", 2L,
                "Luis Muñoz", "OTRO", "Otro", "No sé por qué me cobran esto", null, estado, estado, 9L, "Andrés Lagos",
                null, LocalDateTime.now(), null, null, null, false, List.of());
    }

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(reporteController).build();
    }

    @Test
    void crear_devuelve201() throws Exception {
        when(reporteService.crear(any(CrearReporteRequest.class))).thenReturn(reporte("ABIERTO"));

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compromisoId\":36,\"usuarioId\":2,\"motivo\":\"OTRO\",\"comentario\":\"No sé por qué me cobran esto\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("REP-0012"));
    }

    @Test
    void listarYResumen_respondenOk() throws Exception {
        when(reporteService.listar("ABIERTO", null, null, null, true, 0, 5))
                .thenReturn(new PaginaResponse<>(List.of(reporte("ABIERTO")), 0, 1, 1));
        when(reporteService.resumen()).thenReturn(new ResumenReportesResponse(2, 1, 1, 6, 1));

        mockMvc.perform(get("/api/reportes").param("estado", "ABIERTO").param("soloAtrasados", "true"))
                .andExpect(jsonPath("$.totalElementos").value(1));
        mockMvc.perform(get("/api/reportes/resumen")).andExpect(jsonPath("$.cerrados").value(6));
    }

    @Test
    void obtenerYUltimoDeCompromiso_respondenOk() throws Exception {
        when(reporteService.obtener(12L)).thenReturn(reporte("EN_GESTION"));
        when(reporteService.ultimoDeCompromiso(36L)).thenReturn(reporte("EN_GESTION"));

        mockMvc.perform(get("/api/reportes/12")).andExpect(jsonPath("$.estado").value("EN_GESTION"));
        mockMvc.perform(get("/api/reportes/compromiso/36")).andExpect(status().isOk());
    }

    @Test
    void flujoDelReporte_tomarResolverReabrirYCerrar() throws Exception {
        when(reporteService.tomar(12L, 9L)).thenReturn(reporte("EN_GESTION"));
        when(reporteService.resolver(12L, 9L, "Revisamos tu pago")).thenReturn(reporte("RESUELTO"));
        when(reporteService.reabrir(12L, 2L, "Sigue apareciendo")).thenReturn(reporte("EN_GESTION"));
        when(reporteService.cerrar(12L, 2L)).thenReturn(reporte("CERRADO"));

        mockMvc.perform(patch("/api/reportes/12/tomar").contentType(MediaType.APPLICATION_JSON).content("{\"agenteId\":9}"))
                .andExpect(jsonPath("$.estado").value("EN_GESTION"));
        mockMvc.perform(patch("/api/reportes/12/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agenteId\":9,\"respuesta\":\"Revisamos tu pago\"}"))
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
        mockMvc.perform(patch("/api/reportes/12/reabrir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":2,\"comentario\":\"Sigue apareciendo\"}"))
                .andExpect(jsonPath("$.estado").value("EN_GESTION"));
        mockMvc.perform(patch("/api/reportes/12/cerrar").contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":2}"))
                .andExpect(jsonPath("$.estado").value("CERRADO"));
    }

    @Test
    void transicionNoPermitida_devuelve409() throws Exception {
        when(reporteService.cerrar(12L, 2L))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Un reporte abierto no puede pasar a cerrado"));

        mockMvc.perform(patch("/api/reportes/12/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":2}"))
                .andExpect(status().isConflict());
    }

    @Test
    void foto_subirVerYQuitar() throws Exception {
        when(reporteService.subirFoto(eq(12L), any(MultipartFile.class))).thenReturn(reporte("ABIERTO"));
        when(reporteService.obtenerFoto(12L)).thenReturn(new byte[]{1, 2});
        when(reporteService.eliminarFoto(12L)).thenReturn(reporte("ABIERTO"));

        mockMvc.perform(multipart("/api/reportes/12/foto")
                        .file(new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[]{1})))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reportes/12/foto"))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(new byte[]{1, 2}));
        mockMvc.perform(delete("/api/reportes/12/foto")).andExpect(status().isOk());
    }
}
