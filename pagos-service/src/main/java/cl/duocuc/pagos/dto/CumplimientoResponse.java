package cl.duocuc.pagos.dto;

public record CumplimientoResponse(int considerados, int aTiempo, Double porcentaje, String categoria, String categoriaNombre, String mensaje) {
}
