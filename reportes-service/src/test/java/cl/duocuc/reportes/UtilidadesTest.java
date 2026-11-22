package cl.duocuc.reportes;

import cl.duocuc.reportes.dto.PaginaResponse;
import cl.duocuc.reportes.util.DiasHabiles;
import cl.duocuc.reportes.util.Paginacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilidadesTest {

    @Test
    void diasHabiles_noCuentaSabadosNiDomingos() {
        LocalDate viernes = LocalDate.of(2026, 11, 20);

        assertEquals(1, DiasHabiles.entre(viernes, viernes.plusDays(3)));
        assertEquals(5, DiasHabiles.entre(viernes, viernes.plusDays(7)));
        assertEquals(0, DiasHabiles.entre(viernes, viernes));
    }

    @Test
    void paginacion_cortaLaListaYAjustaLaPaginaFueraDeRango() {
        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7);

        PaginaResponse<Integer> segunda = Paginacion.paginar(numeros, 1, 3);
        PaginaResponse<Integer> fueraDeRango = Paginacion.paginar(numeros, 9, 3);

        assertEquals(List.of(4, 5, 6), segunda.contenido());
        assertEquals(2, fueraDeRango.pagina());
    }
}
