package cl.duocuc.pagos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        String numeroComprobante,
        Long compromisoId,
        Long usuarioId,
        String nombreUsuario,
        String rutUsuario,
        String folio,
        String empresa,
        String razonSocial,
        String concepto,
        Integer numeroCuota,
        Integer totalCuotas,
        int montoCuota,
        LocalDate vencimiento,
        LocalDateTime fechaHora,
        int montoPagado,
        String canal,
        String canalNombre,
        String medioPago,
        boolean aTiempo
) {
}
