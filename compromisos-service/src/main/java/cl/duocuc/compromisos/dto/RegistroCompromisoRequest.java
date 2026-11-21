package cl.duocuc.compromisos.dto;

import java.time.LocalDate;

public record RegistroCompromisoRequest(
        Long empresaId,
        Long ejecutivoId,
        String rut,
        String nombre,
        String correo,
        String telefono,
        String concepto,
        Integer numeroCuota,
        Integer totalCuotas,
        Integer monto,
        LocalDate vencimiento
) {
}
