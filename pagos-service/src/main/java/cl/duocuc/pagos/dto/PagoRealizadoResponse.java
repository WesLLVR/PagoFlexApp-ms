package cl.duocuc.pagos.dto;

public record PagoRealizadoResponse(PagoResponse pago, CompromisoDto siguienteCuota) {
}
