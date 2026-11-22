package cl.duocuc.usuarios;

import cl.duocuc.usuarios.dto.PaginaResponse;
import cl.duocuc.usuarios.util.Paginacion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginacionTest {

    @Test
    void paginar_cortaLaListaYAjustaLaPaginaFueraDeRango() {
        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7);

        PaginaResponse<Integer> segunda = Paginacion.paginar(numeros, 1, 3);
        PaginaResponse<Integer> fueraDeRango = Paginacion.paginar(numeros, 9, 3);
        PaginaResponse<Integer> vacia = Paginacion.paginar(List.of(), 0, 0);

        assertEquals(List.of(4, 5, 6), segunda.contenido());
        assertEquals(3, segunda.totalPaginas());
        assertEquals(2, fueraDeRango.pagina());
        assertEquals(List.of(7), fueraDeRango.contenido());
        assertEquals(1, vacia.totalPaginas());
        assertEquals(0, vacia.totalElementos());
    }
}
