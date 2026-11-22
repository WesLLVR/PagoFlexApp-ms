package cl.duocuc.pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompromisoDto(
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
        long diasAtraso
) {
}
