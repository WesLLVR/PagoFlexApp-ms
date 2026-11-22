package cl.duocuc.compromisos.config;

import cl.duocuc.compromisos.model.Compromiso;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.model.EstadoCompromiso;
import cl.duocuc.compromisos.model.MotivoAnulacion;
import cl.duocuc.compromisos.repository.CompromisoRepository;
import cl.duocuc.compromisos.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final CompromisoRepository compromisoRepository;

    @Override
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();

        Empresa e1 = empresa("EC-01", "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "76.845.213-K", "Inmobiliaria", "atencion@altosmaipo.cl", "+56 2 0000 0201", 8500);
        Empresa e2 = empresa("EC-02", "Colegio Bosque Nativo", "Corporación Educacional Bosque Nativo", "65.132.874-8", "Educación escolar", "pagos@bosquenativo.cl", "+56 2 0000 0202", 5000);
        Empresa e3 = empresa("EC-03", "Crédito Andino", "Crédito Andino S.A.", "96.571.438-3", "Crédito de consumo", "clientes@creditoandino.cl", "+56 2 0000 0203", 12000);
        Empresa e4 = empresa("EC-04", "Horizonte Sur", "Seguros Horizonte Sur S.A.", "96.814.205-4", "Seguros", "contacto@horizontesur.cl", "+56 2 0000 0204", 3000);
        Empresa e5 = empresa("EC-05", "Manos Abiertas", "Fundación Manos Abiertas", "65.247.190-0", "ONG", "socios@manosabiertas.cl", "+56 2 0000 0205", 0);
        Empresa e6 = empresa("EC-06", "Club Los Halcones", "Club Deportivo Los Halcones", "65.398.412-K", "Club deportivo", "tesoreria@loshalcones.cl", "+56 2 0000 0206", 2000);
        Empresa e7 = empresa("EC-07", "Instituto Cordillera", "Instituto Técnico Cordillera SpA", "77.214.583-7", "Educación superior", "finanzas@itcordillera.cl", "+56 2 0000 0207", 6000);
        Empresa e8 = empresa("EC-08", "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", "76.432.871-K", "Administración de arriendos", "cobranza@plazaoriente.cl", "+56 2 0000 0208", 7500);

        compromiso(folio(hoy.minusMonths(1).plusDays(4), 1), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(1).plusDays(4)), 9, 12, 520000, hoy.minusMonths(1).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(1).minusDays(1), 2), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(1).minusDays(1)), 9, 12, 15000, hoy.minusMonths(1).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(1).plusDays(9), 3), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(1).plusDays(9)), null, null, 10000, hoy.minusMonths(1).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(2).plusDays(4), 4), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(2).plusDays(4)), 8, 12, 520000, hoy.minusMonths(2).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(2).minusDays(1), 5), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(2).minusDays(1)), 8, 12, 15000, hoy.minusMonths(2).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(2).plusDays(9), 6), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(2).plusDays(9)), null, null, 10000, hoy.minusMonths(2).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(3).plusDays(4), 7), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(3).plusDays(4)), 7, 12, 520000, hoy.minusMonths(3).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(3).minusDays(1), 8), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(3).minusDays(1)), 7, 12, 15000, hoy.minusMonths(3).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(3).plusDays(9), 9), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(3).plusDays(9)), null, null, 10000, hoy.minusMonths(3).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(4).plusDays(4), 10), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(4).plusDays(4)), 6, 12, 520000, hoy.minusMonths(4).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(4).minusDays(1), 11), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(4).minusDays(1)), 6, 12, 15000, hoy.minusMonths(4).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(4).plusDays(9), 12), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(4).plusDays(9)), null, null, 10000, hoy.minusMonths(4).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(5).plusDays(4), 13), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(5).plusDays(4)), 5, 12, 520000, hoy.minusMonths(5).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(5).minusDays(1), 14), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(5).minusDays(1)), 5, 12, 15000, hoy.minusMonths(5).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(5).plusDays(9), 15), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(5).plusDays(9)), null, null, 10000, hoy.minusMonths(5).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(6).plusDays(4), 16), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(6).plusDays(4)), 4, 12, 520000, hoy.minusMonths(6).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(6).minusDays(1), 17), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(6).minusDays(1)), 4, 12, 15000, hoy.minusMonths(6).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(6).plusDays(9), 18), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(6).plusDays(9)), null, null, 10000, hoy.minusMonths(6).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(7).plusDays(4), 19), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(7).plusDays(4)), 3, 12, 520000, hoy.minusMonths(7).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(7).minusDays(1), 20), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(7).minusDays(1)), 3, 12, 15000, hoy.minusMonths(7).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(7).plusDays(9), 21), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(7).plusDays(9)), null, null, 10000, hoy.minusMonths(7).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(8).plusDays(4), 22), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(8).plusDays(4)), 2, 12, 520000, hoy.minusMonths(8).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(8).minusDays(1), 23), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(8).minusDays(1)), 2, 12, 15000, hoy.minusMonths(8).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(8).plusDays(9), 24), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(8).plusDays(9)), null, null, 10000, hoy.minusMonths(8).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso(folio(hoy.minusMonths(9).plusDays(4), 25), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.minusMonths(9).plusDays(4)), 1, 12, 520000, hoy.minusMonths(9).plusDays(4), EstadoCompromiso.PAGADO, 20L);
        compromiso(folio(hoy.minusMonths(9).minusDays(1), 26), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusMonths(9).minusDays(1)), 1, 12, 15000, hoy.minusMonths(9).minusDays(1), EstadoCompromiso.PAGADO, 18L);
        compromiso(folio(hoy.minusMonths(9).plusDays(9), 27), 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.minusMonths(9).plusDays(9)), null, null, 10000, hoy.minusMonths(9).plusDays(9), EstadoCompromiso.PAGADO, null);
        compromiso("CP-2026-0098", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e3, "Crédito de consumo", "Crédito de consumo", 6, 24, 86400, hoy.minusDays(56), EstadoCompromiso.PAGADO, 16L);
        compromiso("CP-2026-0104", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e3, "Crédito de consumo", "Crédito de consumo", 7, 24, 86400, hoy.minusDays(26), EstadoCompromiso.PAGADO, 16L);
        compromiso("CP-2026-0101", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e8, "Arriendo departamento", conMes("Arriendo departamento", hoy.plusDays(4)), 10, 12, 520000, hoy.plusDays(4), EstadoCompromiso.PENDIENTE, 20L);
        compromiso("CP-2026-0102", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.minusDays(1)), 10, 12, 15000, hoy.minusDays(1), EstadoCompromiso.PENDIENTE, 18L);
        compromiso("CP-2026-0103", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e5, "Aporte mensual", conMes("Aporte mensual", hoy.plusDays(9)), null, null, 10000, hoy.plusDays(9), EstadoCompromiso.PENDIENTE, null);
        compromiso("CP-2026-0105", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e3, "Crédito de consumo", "Crédito de consumo", 8, 24, 86400, hoy.plusDays(19), EstadoCompromiso.PENDIENTE, 16L);
        compromiso("CP-2026-0113", 1L, "Camila Andrea Rojas Peña", "15.834.207-3", e6, "Cuota social", conMes("Cuota social", hoy.plusDays(29)), 11, 12, 15000, hoy.plusDays(29), EstadoCompromiso.PENDIENTE, 18L);
        compromiso("CP-2026-0072", 2L, "Luis Alberto Muñoz Tapia", "12.476.318-5", e1, "Cuota pie departamento", "Cuota pie departamento", 13, 36, 310000, hoy.minusDays(36), EstadoCompromiso.PAGADO, 14L);
        compromiso("CP-2026-0106", 2L, "Luis Alberto Muñoz Tapia", "12.476.318-5", e1, "Cuota pie departamento", "Cuota pie departamento", 14, 36, 310000, hoy.minusDays(6), EstadoCompromiso.EN_REVISION, 14L);
        compromiso("CP-2026-0107", 2L, "Luis Alberto Muñoz Tapia", "12.476.318-5", e1, "Cuota pie departamento", "Cuota pie departamento", 15, 36, 310000, hoy.plusDays(24), EstadoCompromiso.PENDIENTE, 14L);
        compromiso(folio(hoy.plusDays(5), 201), 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", e1, "Cuota pie departamento", "Cuota pie departamento", 20, 36, 290000, hoy.plusDays(5), EstadoCompromiso.PENDIENTE, 14L);
        compromiso(folio(hoy.minusMonths(1).plusDays(5), 202), 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", e1, "Cuota pie departamento", "Cuota pie departamento", 19, 36, 290000, hoy.minusMonths(1).plusDays(5), EstadoCompromiso.PAGADO, 14L);
        compromiso(folio(hoy.minusMonths(2).plusDays(5), 203), 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", e1, "Cuota pie departamento", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).plusDays(5), EstadoCompromiso.PAGADO, 14L);
        compromiso(folio(hoy.minusDays(10), 204), 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", e1, "Cuota pie departamento", "Cuota pie departamento", 20, 36, 290000, hoy.minusDays(10), EstadoCompromiso.PENDIENTE, 14L);
        compromiso(folio(hoy.minusMonths(1).minusDays(10), 205), 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", e1, "Cuota pie departamento", "Cuota pie departamento", 19, 36, 290000, hoy.minusMonths(1).minusDays(10), EstadoCompromiso.PAGADO, 14L);
        compromiso(folio(hoy.minusMonths(2).minusDays(10), 206), 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", e1, "Cuota pie departamento", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).minusDays(10), EstadoCompromiso.PAGADO, 14L);
        compromiso(folio(hoy.plusDays(15), 207), 8L, "Benjamín Esteban Araya Fuentes", "18.704.396-4", e1, "Cuota pie departamento", "Cuota pie departamento", 20, 36, 290000, hoy.plusDays(15), EstadoCompromiso.PENDIENTE, 14L);
        anulado(compromiso(folio(hoy.minusMonths(1).plusDays(15), 208), 8L, "Benjamín Esteban Araya Fuentes", "18.704.396-4", e1, "Cuota pie departamento", "Cuota pie departamento", 19, 36, 290000, hoy.minusMonths(1).plusDays(15), EstadoCompromiso.ANULADO, 14L), MotivoAnulacion.REPACTACION, 14L, hoy.minusMonths(1).plusDays(15).minusDays(2));
        compromiso(folio(hoy.minusMonths(2).plusDays(15), 209), 8L, "Benjamín Esteban Araya Fuentes", "18.704.396-4", e1, "Cuota pie departamento", "Cuota pie departamento", 18, 36, 290000, hoy.minusMonths(2).plusDays(15), EstadoCompromiso.PAGADO, 14L);
        compromiso("CP-2026-0108", 3L, "Rosa Elena Contreras Díaz", "6.893.541-5", e4, "Seguro de hogar", "Seguro de hogar", 10, 12, 18900, hoy.minusDays(23), EstadoCompromiso.PAGADO, null);
        compromiso("CP-2026-0109", 4L, "Matías Ignacio Soto Vergara", "21.045.873-5", e7, "Arancel", "Arancel", 8, 10, 142000, hoy.minusDays(19), EstadoCompromiso.EN_REVISION, 19L);
        compromiso("CP-2026-0110", 5L, "Fernanda Paz Godoy Lagos", "17.562.904-1", e2, "Mensualidad", "Mensualidad", 8, 10, 185000, hoy.plusDays(4), EstadoCompromiso.PENDIENTE, 15L);
        anulado(compromiso("CP-2026-0111", 6L, "Héctor Manuel Pizarro Campos", "10.318.265-4", e3, "Crédito de consumo", "Crédito de consumo", 19, 36, 54200, hoy.minusDays(34), EstadoCompromiso.ANULADO, 17L), MotivoAnulacion.REPACTACION, 17L, hoy.minusDays(32));
        compromiso("CP-2026-0112", 7L, "Javiera Constanza Reyes Molina", "19.287.430-0", e6, "Cuota social", "Cuota social", 10, 12, 15000, hoy.minusDays(1), EstadoCompromiso.PAGADO, 18L);
    }

    private Empresa empresa(String codigo, String nombre, String razonSocial, String rut, String rubro,
                            String correo, String telefono, int recargo) {
        return empresaRepository.save(Empresa.builder()
                .codigo(codigo).nombre(nombre).razonSocial(razonSocial).rut(rut).rubro(rubro)
                .correo(correo).telefono(telefono).recargo(recargo)
                .build());
    }

    private Compromiso compromiso(String folio, Long usuarioId, String nombre, String rut, Empresa empresa, String deuda,
                                  String concepto, Integer cuota, Integer total, int monto, LocalDate vencimiento,
                                  EstadoCompromiso estado, Long ejecutivoId) {
        return compromisoRepository.save(Compromiso.builder()
                .folio(folio).usuarioId(usuarioId).nombreUsuario(nombre).rutUsuario(rut).empresa(empresa)
                .deuda(deuda).concepto(concepto).numeroCuota(cuota).totalCuotas(total).monto(monto)
                .vencimiento(vencimiento).estado(estado).ejecutivoRegistroId(ejecutivoId)
                .fechaRegistro(vencimiento.minusDays(25))
                .build());
    }

    private void anulado(Compromiso compromiso, MotivoAnulacion motivo, Long ejecutivoId, LocalDate fecha) {
        compromiso.setMotivoAnulacion(motivo);
        compromiso.setComentarioAnulacion("");
        compromiso.setEjecutivoAnulacionId(ejecutivoId);
        compromiso.setFechaAnulacion(fecha);
        compromisoRepository.save(compromiso);
    }

    private String folio(LocalDate fecha, int numero) {
        return String.format(Locale.ROOT, "CP-%d-%04d", fecha.getYear(), numero);
    }

    private String conMes(String deuda, LocalDate fecha) {
        return deuda + " " + fecha.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL"));
    }
}
