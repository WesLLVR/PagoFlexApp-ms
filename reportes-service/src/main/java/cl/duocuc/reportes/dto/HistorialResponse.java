package cl.duocuc.reportes.dto;

import java.time.LocalDateTime;

public record HistorialResponse(
        String estadoAnterior,
        String estadoNuevo,
        String estadoNombre,
        LocalDateTime fecha,
        String autor,
        String comentario
) {
}
