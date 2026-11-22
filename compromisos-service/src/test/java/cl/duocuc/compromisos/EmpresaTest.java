package cl.duocuc.compromisos;

import cl.duocuc.compromisos.controller.EmpresaController;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.repository.EmpresaRepository;
import cl.duocuc.compromisos.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmpresaTest {

    @Mock
    private EmpresaRepository empresaRepository;

    private final Empresa club = Empresa.builder().id(6L).codigo("EC-06").nombre("Club Los Halcones")
            .razonSocial("Club Deportivo Los Halcones").recargo(2000).build();

    @Test
    void listarYObtener_porElControlador() throws Exception {
        when(empresaRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(club));
        when(empresaRepository.findById(6L)).thenReturn(Optional.of(club));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new EmpresaController(new EmpresaService(empresaRepository))).build();

        mockMvc.perform(get("/api/empresas")).andExpect(jsonPath("$[0].codigo").value("EC-06"));
        mockMvc.perform(get("/api/empresas/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recargo").value(2000));
    }

    @Test
    void buscar_empresaInexistente_responde404() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());
        EmpresaService empresaService = new EmpresaService(empresaRepository);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> empresaService.buscar(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
