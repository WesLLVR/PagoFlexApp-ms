package cl.duocuc.pagos.config;

import cl.duocuc.pagos.model.Canal;
import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final PagoRepository pagoRepository;

    @Override
    public void run(String... args) {
        if (pagoRepository.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();

        pago("CPR-000300", 1L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(1).plusDays(4), 1), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(1).plusDays(4)), 9, 12, 520000, hoy.minusMonths(1).plusDays(4), hoy.minusMonths(1).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000301", 2L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(1).minusDays(1), 2), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(1).minusDays(1)), 9, 12, 15000, hoy.minusMonths(1).minusDays(1), hoy.minusMonths(1).minusDays(1).atTime(9, 15), 15000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000302", 3L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(1).plusDays(9), 3), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(1).plusDays(9)), null, null, 10000, hoy.minusMonths(1).plusDays(9), hoy.minusMonths(1).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000303", 4L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(2).plusDays(4), 4), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(2).plusDays(4)), 8, 12, 520000, hoy.minusMonths(2).plusDays(4), hoy.minusMonths(2).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.APP, "Transferencia", true);
        pago("CPR-000304", 5L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(2).minusDays(1), 5), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(2).minusDays(1)), 8, 12, 15000, hoy.minusMonths(2).minusDays(1), hoy.minusMonths(2).minusDays(1).plusDays(2).atTime(18, 40), 17000, Canal.APP, "Transferencia", false);
        pago("CPR-000305", 6L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(2).plusDays(9), 6), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(2).plusDays(9)), null, null, 10000, hoy.minusMonths(2).plusDays(9), hoy.minusMonths(2).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.APP, "Transferencia", true);
        pago("CPR-000306", 7L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(3).plusDays(4), 7), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(3).plusDays(4)), 7, 12, 520000, hoy.minusMonths(3).plusDays(4), hoy.minusMonths(3).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000307", 8L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(3).minusDays(1), 8), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(3).minusDays(1)), 7, 12, 15000, hoy.minusMonths(3).minusDays(1), hoy.minusMonths(3).minusDays(1).atTime(9, 15), 15000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000308", 9L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(3).plusDays(9), 9), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(3).plusDays(9)), null, null, 10000, hoy.minusMonths(3).plusDays(9), hoy.minusMonths(3).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000309", 10L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(4).plusDays(4), 10), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(4).plusDays(4)), 6, 12, 520000, hoy.minusMonths(4).plusDays(4), hoy.minusMonths(4).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000310", 11L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(4).minusDays(1), 11), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(4).minusDays(1)), 6, 12, 15000, hoy.minusMonths(4).minusDays(1), hoy.minusMonths(4).minusDays(1).atTime(9, 15), 15000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000311", 12L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(4).plusDays(9), 12), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(4).plusDays(9)), null, null, 10000, hoy.minusMonths(4).plusDays(9), hoy.minusMonths(4).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000312", 13L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(5).plusDays(4), 13), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(5).plusDays(4)), 5, 12, 520000, hoy.minusMonths(5).plusDays(4), hoy.minusMonths(5).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.APP, "Transferencia", true);
        pago("CPR-000313", 14L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(5).minusDays(1), 14), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(5).minusDays(1)), 5, 12, 15000, hoy.minusMonths(5).minusDays(1), hoy.minusMonths(5).minusDays(1).plusDays(2).atTime(18, 40), 17000, Canal.APP, "Transferencia", false);
        pago("CPR-000314", 15L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(5).plusDays(9), 15), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(5).plusDays(9)), null, null, 10000, hoy.minusMonths(5).plusDays(9), hoy.minusMonths(5).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.APP, "Transferencia", true);
        pago("CPR-000315", 16L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(6).plusDays(4), 16), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(6).plusDays(4)), 4, 12, 520000, hoy.minusMonths(6).plusDays(4), hoy.minusMonths(6).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000316", 17L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(6).minusDays(1), 17), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(6).minusDays(1)), 4, 12, 15000, hoy.minusMonths(6).minusDays(1), hoy.minusMonths(6).minusDays(1).atTime(9, 15), 15000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000317", 18L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(6).plusDays(9), 18), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(6).plusDays(9)), null, null, 10000, hoy.minusMonths(6).plusDays(9), hoy.minusMonths(6).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000318", 19L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(7).plusDays(4), 19), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(7).plusDays(4)), 3, 12, 520000, hoy.minusMonths(7).plusDays(4), hoy.minusMonths(7).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000319", 20L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(7).minusDays(1), 20), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(7).minusDays(1)), 3, 12, 15000, hoy.minusMonths(7).minusDays(1), hoy.minusMonths(7).minusDays(1).atTime(9, 15), 15000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000320", 21L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(7).plusDays(9), 21), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(7).plusDays(9)), null, null, 10000, hoy.minusMonths(7).plusDays(9), hoy.minusMonths(7).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.AGENTE, "Tarjeta de crédito", true);
        pago("CPR-000321", 22L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(8).plusDays(4), 22), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(8).plusDays(4)), 2, 12, 520000, hoy.minusMonths(8).plusDays(4), hoy.minusMonths(8).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.APP, "Transferencia", true);
        pago("CPR-000322", 23L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(8).minusDays(1), 23), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(8).minusDays(1)), 2, 12, 15000, hoy.minusMonths(8).minusDays(1), hoy.minusMonths(8).minusDays(1).atTime(9, 15), 15000, Canal.APP, "Transferencia", true);
        pago("CPR-000323", 24L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(8).plusDays(9), 24), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(8).plusDays(9)), null, null, 10000, hoy.minusMonths(8).plusDays(9), hoy.minusMonths(8).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.APP, "Transferencia", true);
        pago("CPR-000324", 25L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(9).plusDays(4), 25), "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", conMes("Arriendo departamento", hoy.minusMonths(9).plusDays(4)), 1, 12, 520000, hoy.minusMonths(9).plusDays(4), hoy.minusMonths(9).plusDays(4).minusDays(1).atTime(13, 5), 520000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000325", 26L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(9).minusDays(1), 26), "Club Los Halcones", "Club Deportivo Los Halcones", conMes("Cuota social", hoy.minusMonths(9).minusDays(1)), 1, 12, 15000, hoy.minusMonths(9).minusDays(1), hoy.minusMonths(9).minusDays(1).atTime(9, 15), 15000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000326", 27L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", folio(hoy.minusMonths(9).plusDays(9), 27), "Manos Abiertas", "Fundación Manos Abiertas", conMes("Aporte mensual", hoy.minusMonths(9).plusDays(9)), null, null, 10000, hoy.minusMonths(9).plusDays(9), hoy.minusMonths(9).plusDays(9).minusDays(3).atTime(8, 22), 10000, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000327", 28L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", "CP-2026-0098", "Crédito Andino", "Crédito Andino S.A.", "Crédito de consumo", 6, 24, 86400, hoy.minusDays(56), hoy.minusDays(57).atTime(11, 30), 86400, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000451", 29L, 1L, "Camila Andrea Rojas Peña", "15.834.207-3", "CP-2026-0104", "Crédito Andino", "Crédito Andino S.A.", "Crédito de consumo", 7, 24, 86400, hoy.minusDays(26), hoy.minusDays(27).atTime(10, 42), 86400, Canal.PORTAL, "Tarjeta de débito", true);
        pago("CPR-000377", 35L, 2L, "Luis Alberto Muñoz Tapia", "12.476.318-5", "CP-2026-0072", "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 13, 36, 310000, hoy.minusDays(36), hoy.minusDays(34).atTime(21, 14), 318500, Canal.AGENTE, "Transferencia", false);
        pago("CPR-000328", 39L, 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", folio(hoy.minusMonths(1).plusDays(5), 202), "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 19, 36, 290000, hoy.minusMonths(1).plusDays(5), hoy.minusMonths(1).plusDays(5).plusDays(3).atTime(19, 20), 298500, Canal.PORTAL, "Tarjeta de crédito", false);
        pago("CPR-000329", 40L, 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", folio(hoy.minusMonths(2).plusDays(5), 203), "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).plusDays(5), hoy.minusMonths(2).plusDays(5).minusDays(1).atTime(12, 10), 290000, Canal.APP, "Tarjeta de débito", true);
        pago("CPR-000330", 42L, 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", folio(hoy.minusMonths(1).minusDays(10), 205), "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 19, 36, 290000, hoy.minusMonths(1).minusDays(10), hoy.minusMonths(1).minusDays(10).minusDays(1).atTime(12, 10), 290000, Canal.APP, "Tarjeta de débito", true);
        pago("CPR-000331", 43L, 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", folio(hoy.minusMonths(2).minusDays(10), 206), "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).minusDays(10), hoy.minusMonths(2).minusDays(10).minusDays(1).atTime(12, 10), 290000, Canal.APP, "Tarjeta de débito", true);
        pago("CPR-000332", 46L, 8L, "Benjamín Esteban Araya Fuentes", "18.704.396-4", folio(hoy.minusMonths(2).plusDays(15), 209), "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).plusDays(15), hoy.minusMonths(2).plusDays(15).minusDays(1).atTime(12, 10), 290000, Canal.APP, "Tarjeta de débito", true);
        pago("CPR-000452", 47L, 3L, "Rosa Elena Contreras Díaz", "6.893.541-5", "CP-2026-0108", "Horizonte Sur", "Seguros Horizonte Sur S.A.", "Seguro de hogar", 10, 12, 18900, hoy.minusDays(23), hoy.minusDays(24).atTime(19, 5), 18900, Canal.AGENTE, "Tarjeta de débito", true);
        pago("CPR-000453", 51L, 7L, "Javiera Constanza Reyes Molina", "19.287.430-0", "CP-2026-0112", "Club Los Halcones", "Club Deportivo Los Halcones", "Cuota social", 10, 12, 15000, hoy.minusDays(1), hoy.minusDays(2).atTime(8, 15), 15000, Canal.APP, "Tarjeta de débito", true);
    }

    private void pago(String numero, Long compromisoId, Long usuarioId, String nombre, String rut, String folio,
                      String empresa, String razonSocial, String concepto, Integer cuota, Integer total, int montoCuota,
                      LocalDate vencimiento, LocalDateTime fechaHora, int montoPagado, Canal canal, String medio,
                      boolean aTiempo) {
        pagoRepository.save(Pago.builder()
                .numeroComprobante(numero).compromisoId(compromisoId).usuarioId(usuarioId)
                .nombreUsuario(nombre).rutUsuario(rut).folio(folio).empresa(empresa).razonSocial(razonSocial)
                .concepto(concepto).numeroCuota(cuota).totalCuotas(total).montoCuota(montoCuota)
                .vencimiento(vencimiento).fechaHora(fechaHora).montoPagado(montoPagado).canal(canal)
                .medioPago(medio).aTiempo(aTiempo)
                .build());
    }

    private String folio(LocalDate fecha, int numero) {
        return String.format(Locale.ROOT, "CP-%d-%04d", fecha.getYear(), numero);
    }

    private String conMes(String deuda, LocalDate fecha) {
        return deuda + " " + fecha.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL"));
    }
}
