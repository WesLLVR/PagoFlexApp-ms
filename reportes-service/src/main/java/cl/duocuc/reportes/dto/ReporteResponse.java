package cl.duocuc.reportes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReporteResponse(
        Long id,
        String numero,
        Long compromisoId,
        String folio,
        String concepto,
        String empresa,
        Long usuarioId,
        String nombreUsuario,
        String motivo,
        String motivoNombre,
        String comentario,
        String fotoUrl,
        String estado,
        String estadoNombre,
        Long agenteId,
        String nombreAgente,
        String respuesta,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaToma,
        LocalDateTime fechaResolucion,
        LocalDateTime fechaCierre,
        boolean atrasado,
        List<HistorialResponse> historial
) {
}
