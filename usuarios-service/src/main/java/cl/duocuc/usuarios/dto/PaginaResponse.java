package cl.duocuc.usuarios.dto;

import java.util.List;

public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int totalPaginas,
        long totalElementos
) {
}
