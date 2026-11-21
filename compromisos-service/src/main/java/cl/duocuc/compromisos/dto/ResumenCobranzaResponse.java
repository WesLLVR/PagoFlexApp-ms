package cl.duocuc.compromisos.dto;

public record ResumenCobranzaResponse(String mes, int pagados, int pendientes, int vencidos, int enRevision, int anulados, long montoRecaudado, Double tasa) {
}
