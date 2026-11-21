package cl.duocuc.compromisos.dto;

import java.time.LocalDate;

public record CompromisoResponse(
        Long id,
        String folio,
        Long usuarioId,
        String nombreUsuario,
        String rutUsuario,
        Long empresaId,
        String empresa,
        String razonSocial,
        String deuda,
        String concepto,
        Integer numeroCuota,
        Integer totalCuotas,
        int monto,
        int recargo,
        int totalAPagar,
        LocalDate vencimiento,
        String estado,
        long diasAtraso,
        String motivoAnulacion,
        String comentarioAnulacion
) {
}
