package cl.duocuc.reportes.dto;

public record CrearReporteRequest(Long compromisoId, Long usuarioId, String motivo, String comentario) {
}
