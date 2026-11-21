package cl.duocuc.compromisos.dto;

import java.time.LocalDate;

public record ResumenMesResponse(int totalMes, LocalDate proximoVencimiento, String empresaProximo, int cantidadVencidos) {
}
