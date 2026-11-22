package cl.duocuc.reportes.config;

import cl.duocuc.reportes.model.EstadoReporte;
import cl.duocuc.reportes.model.HistorialEstado;
import cl.duocuc.reportes.model.MotivoReporte;
import cl.duocuc.reportes.model.Reporte;
import cl.duocuc.reportes.repository.HistorialEstadoRepository;
import cl.duocuc.reportes.repository.ReporteRepository;
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

    private final ReporteRepository reporteRepository;
    private final HistorialEstadoRepository historialRepository;

    @Override
    public void run(String... args) {
        if (reporteRepository.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();

        reporte("REP-0001", 1L, folio(hoy.minusMonths(1).plusDays(4), 1), conMes("Arriendo departamento", hoy.minusMonths(1).plusDays(4)), "Plaza Oriente", 1L, "Camila Andrea Rojas Peña", MotivoReporte.YA_PAGUE, "Pagué el arriendo en la oficina y sigue apareciendo pendiente.", hoy.minusMonths(1).plusDays(5), EstadoReporte.CERRADO, 9L, "Andrés Felipe Lagos Meza", "Confirmamos tu pago con Plaza Oriente. Ya quedó registrado.");
        reporte("REP-0002", 5L, folio(hoy.minusMonths(2).minusDays(1), 5), conMes("Cuota social", hoy.minusMonths(2).minusDays(1)), "Club Los Halcones", 1L, "Camila Andrea Rojas Peña", MotivoReporte.MONTO_INCORRECTO, "La cuota social me aparece con recargo y la pagué a tiempo.", hoy.minusMonths(2).plusDays(1), EstadoReporte.CERRADO, 10L, "Daniela Sofía Ortiz Bravo", "El recargo se aplicó porque el pago llegó dos días después del vencimiento.");
        reporte("REP-0003", 9L, folio(hoy.minusMonths(3).plusDays(9), 9), conMes("Aporte mensual", hoy.minusMonths(3).plusDays(9)), "Manos Abiertas", 1L, "Camila Andrea Rojas Peña", MotivoReporte.NO_RECONOZCO, "No reconozco este aporte, nunca me inscribí.", hoy.minusMonths(3).plusDays(10), EstadoReporte.CERRADO, 9L, "Andrés Felipe Lagos Meza", "La fundación confirmó tu inscripción de marzo. Puedes darte de baja con ellos.");
        reporte("REP-0004", 13L, folio(hoy.minusMonths(5).plusDays(4), 13), conMes("Arriendo departamento", hoy.minusMonths(5).plusDays(4)), "Plaza Oriente", 1L, "Camila Andrea Rojas Peña", MotivoReporte.YA_PAGUE, "Lo pagué por transferencia y no se ve.", hoy.minusMonths(4).plusDays(5), EstadoReporte.CERRADO, 10L, "Daniela Sofía Ortiz Bravo", "La transferencia se acreditó al día siguiente. Ya está al día.");
        reporte("REP-0005", 17L, folio(hoy.minusMonths(6).minusDays(1), 17), conMes("Cuota social", hoy.minusMonths(6).minusDays(1)), "Club Los Halcones", 1L, "Camila Andrea Rojas Peña", MotivoReporte.OTRO, "El comprobante no muestra el número de cuota correcto.", hoy.minusMonths(5).plusDays(2), EstadoReporte.CERRADO, 9L, "Andrés Felipe Lagos Meza", "Corregimos el número de cuota en el comprobante.");
        reporte("REP-0006", 21L, folio(hoy.minusMonths(7).plusDays(9), 21), conMes("Aporte mensual", hoy.minusMonths(7).plusDays(9)), "Manos Abiertas", 1L, "Camila Andrea Rojas Peña", MotivoReporte.MONTO_INCORRECTO, "El monto del arriendo no coincide con mi contrato.", hoy.minusMonths(6).plusDays(6), EstadoReporte.CERRADO, 10L, "Daniela Sofía Ortiz Bravo", "Plaza Oriente confirmó que el monto corresponde al reajuste anual.");
        reporte("REP-0009", 50L, "CP-2026-0111", "Crédito de consumo", "Crédito Andino", 6L, "Héctor Manuel Pizarro Campos", MotivoReporte.MONTO_INCORRECTO, "Repacté el crédito y me siguen cobrando la cuota antigua.", hoy.minusDays(36), EstadoReporte.RESUELTO, 9L, "Andrés Felipe Lagos Meza", "La empresa anuló la cuota por repactación.");
        reporte("REP-0011", 48L, "CP-2026-0109", "Arancel", "Instituto Cordillera", 4L, "Matías Ignacio Soto Vergara", MotivoReporte.YA_PAGUE, "Pagué por transferencia directa al instituto.", hoy.minusDays(17), EstadoReporte.EN_GESTION, 10L, "Daniela Sofía Ortiz Bravo", null);
        reporte("REP-0012", 36L, "CP-2026-0106", "Cuota pie departamento", "Altos del Maipo", 2L, "Luis Alberto Muñoz Tapia", MotivoReporte.MONTO_INCORRECTO, "No sé por qué me cobran $ 318.500.", hoy.minusDays(5), EstadoReporte.ABIERTO, null, null, null);
    }

    private void reporte(String numero, Long compromisoId, String folio, String concepto, String empresa, Long usuarioId,
                         String nombreUsuario, MotivoReporte motivo, String comentario, LocalDate fecha,
                         EstadoReporte estado, Long agenteId, String nombreAgente, String respuesta) {
        LocalDateTime creado = fecha.atTime(10, 0);
        LocalDateTime tomado = agenteId == null ? null : fecha.plusDays(1).atTime(9, 30);
        LocalDateTime resuelto = respuesta == null ? null : fecha.plusDays(3).atTime(16, 0);
        LocalDateTime cerrado = estado == EstadoReporte.CERRADO ? fecha.plusDays(4).atTime(12, 0) : null;

        Reporte reporte = reporteRepository.save(Reporte.builder()
                .numero(numero).compromisoId(compromisoId).folio(folio).concepto(concepto).empresa(empresa)
                .usuarioId(usuarioId).nombreUsuario(nombreUsuario).motivo(motivo).comentario(comentario)
                .estado(estado).agenteId(agenteId).nombreAgente(nombreAgente).respuesta(respuesta)
                .fechaCreacion(creado).fechaToma(tomado).fechaResolucion(resuelto).fechaCierre(cerrado)
                .build());

        paso(reporte.getId(), null, EstadoReporte.ABIERTO, creado, nombreUsuario, comentario);
        if (tomado != null) {
            paso(reporte.getId(), EstadoReporte.ABIERTO, EstadoReporte.EN_GESTION, tomado, nombreAgente, null);
        }
        if (resuelto != null) {
            paso(reporte.getId(), EstadoReporte.EN_GESTION, EstadoReporte.RESUELTO, resuelto, nombreAgente, respuesta);
        }
        if (cerrado != null) {
            paso(reporte.getId(), EstadoReporte.RESUELTO, EstadoReporte.CERRADO, cerrado, nombreUsuario, null);
        }
    }

    private void paso(Long reporteId, EstadoReporte anterior, EstadoReporte nuevo, LocalDateTime fecha, String autor,
                      String comentario) {
        historialRepository.save(HistorialEstado.builder()
                .reporteId(reporteId).estadoAnterior(anterior).estadoNuevo(nuevo)
                .fecha(fecha).autor(autor).comentario(comentario)
                .build());
    }

    private String folio(LocalDate fecha, int numero) {
        return String.format(Locale.ROOT, "CP-%d-%04d", fecha.getYear(), numero);
    }

    private String conMes(String deuda, LocalDate fecha) {
        return deuda + " " + fecha.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL"));
    }
}
