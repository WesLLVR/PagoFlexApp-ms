package cl.duocuc.compromisos.util;

import cl.duocuc.compromisos.dto.PaginaResponse;

import java.util.List;

public final class Paginacion {

    private static final int TAMANO_MAXIMO = 50;

    private Paginacion() {
    }

    public static <T> PaginaResponse<T> paginar(List<T> lista, int pagina, int tamano) {
        int porPagina = Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
        int totalPaginas = Math.max(1, (int) Math.ceil(lista.size() / (double) porPagina));
        int actual = Math.max(0, Math.min(pagina, totalPaginas - 1));
        int desde = actual * porPagina;
        int hasta = Math.min(desde + porPagina, lista.size());
        return new PaginaResponse<>(lista.subList(desde, hasta), actual, totalPaginas, lista.size());
    }
}
