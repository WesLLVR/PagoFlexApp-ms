package cl.duocuc.pagos.service;

import cl.duocuc.pagos.client.CompromisoClient;
import cl.duocuc.pagos.dto.CompromisoDto;
import cl.duocuc.pagos.dto.CumplimientoResponse;
import cl.duocuc.pagos.dto.PagableDto;
import cl.duocuc.pagos.dto.PagoRealizadoResponse;
import cl.duocuc.pagos.dto.PagoRequest;
import cl.duocuc.pagos.dto.PagoResponse;
import cl.duocuc.pagos.dto.PaginaResponse;
import cl.duocuc.pagos.model.Canal;
import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.repository.PagoRepository;
import cl.duocuc.pagos.util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PagoService {

    private static final Set<String> MEDIOS_DE_PAGO = Set.of("Tarjeta de débito", "Tarjeta de crédito", "Transferencia");
    private static final Set<String> ESTADOS_PAGABLES = Set.of("PENDIENTE", "VENCIDO");
    private static final int MINIMO_PARA_CUMPLIMIENTO = 3;

    private final PagoRepository pagoRepository;
    private final CompromisoClient compromisoClient;

    public PagoRealizadoResponse pagar(PagoRequest datos) {
        if (datos.medioPago() == null || !MEDIOS_DE_PAGO.contains(datos.medioPago())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige cómo quieres pagar");
        }
        if (datos.compromisoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el compromiso a pagar");
        }
        if (pagoRepository.existsByCompromisoId(datos.compromisoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este compromiso ya está pagado.");
        }

        CompromisoDto compromiso = compromisoClient.obtener(datos.compromisoId());
        PagableDto pagable = compromisoClient.pagable(datos.compromisoId());
        if (!pagable.pagable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, pagable.motivo());
        }

        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
        Pago pago = pagoRepository.save(Pago.builder()
                .numeroComprobante(siguienteComprobante())
                .compromisoId(compromiso.id())
                .usuarioId(compromiso.usuarioId())
                .nombreUsuario(compromiso.nombreUsuario())
                .rutUsuario(compromiso.rutUsuario())
                .folio(compromiso.folio())
                .empresa(compromiso.empresa())
                .razonSocial(compromiso.razonSocial())
                .concepto(compromiso.concepto())
                .numeroCuota(compromiso.numeroCuota())
                .totalCuotas(compromiso.totalCuotas())
                .montoCuota(compromiso.monto())
                .vencimiento(compromiso.vencimiento())
                .fechaHora(ahora)
                .montoPagado(compromiso.totalAPagar())
                .canal(Canal.APP)
                .medioPago(datos.medioPago())
                .aTiempo(!ahora.toLocalDate().isAfter(compromiso.vencimiento()))
                .build());

        try {
            compromisoClient.marcarPagado(compromiso.id());
        } catch (ResponseStatusException e) {
            pagoRepository.delete(pago);
            throw e;
        }

        return new PagoRealizadoResponse(aRespuesta(pago), siguienteCuota(compromiso));
    }

    public PaginaResponse<PagoResponse> listar(Long usuarioId, String empresa, Integer anio, LocalDate desde,
                                               LocalDate hasta, String buscar, int pagina, int tamano) {
        List<Pago> base = usuarioId == null
                ? pagoRepository.findAllByOrderByFechaHoraDesc()
                : pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);

        List<PagoResponse> resultado = base.stream()
                .filter(p -> vacio(empresa) || p.getEmpresa().equalsIgnoreCase(empresa))
                .filter(p -> anio == null || p.getFechaHora().getYear() == anio)
                .filter(p -> desde == null || !p.getFechaHora().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFechaHora().toLocalDate().isAfter(hasta))
                .filter(p -> vacio(buscar) || coincide(p, buscar))
                .map(this::aRespuesta)
                .toList();
        return Paginacion.paginar(resultado, pagina, tamano);
    }

    public List<String> empresas(Long usuarioId) {
        return pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId).stream()
                .map(Pago::getEmpresa)
                .distinct()
                .sorted()
                .toList();
    }

    public PagoResponse obtener(Long id) {
        return pagoRepository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
    }

    public PagoResponse porCompromiso(Long compromisoId) {
        return pagoRepository.findByCompromisoId(compromisoId)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Este compromiso no tiene pago"));
    }

    public CumplimientoResponse cumplimiento(Long usuarioId) {
        List<CompromisoDto> considerados = compromisoClient.considerados(usuarioId);
        Map<Long, Boolean> pagosATiempo = pagoRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId).stream()
                .collect(Collectors.toMap(Pago::getCompromisoId, Pago::getATiempo));

        int aTiempo = (int) considerados.stream()
                .filter(c -> Boolean.TRUE.equals(pagosATiempo.get(c.id())))
                .count();
        Double porcentaje = considerados.size() < MINIMO_PARA_CUMPLIMIENTO ? null : aTiempo * 100.0 / considerados.size();

        String categoria;
        String nombre;
        String mensaje;
        if (porcentaje == null) {
            categoria = "SIN_HISTORIAL";
            nombre = "Historial insuficiente";
            mensaje = "Aún no tienes pagos suficientes para calcular tu indicador.";
        } else if (porcentaje >= 95.0) {
            categoria = "EJEMPLAR";
            nombre = "Ejemplar";
            mensaje = "¡Excelente! Tus pagos están impecables.";
        } else if (porcentaje >= 80.0) {
            categoria = "AL_DIA";
            nombre = "Al día";
            mensaje = "Vas muy bien, sigue así.";
        } else {
            categoria = "EN_CAMINO";
            nombre = "En camino";
            mensaje = "Cada pago a tiempo suma. ¡Tú puedes!";
        }
        return new CumplimientoResponse(considerados.size(), aTiempo, porcentaje, categoria, nombre, mensaje);
    }

    private CompromisoDto siguienteCuota(CompromisoDto pagado) {
        return compromisoClient.cuotas(pagado.id()).stream()
                .filter(c -> !c.id().equals(pagado.id()))
                .filter(c -> ESTADOS_PAGABLES.contains(c.estado()))
                .min(Comparator.comparing(CompromisoDto::vencimiento))
                .orElse(null);
    }

    private String siguienteComprobante() {
        int ultimo = pagoRepository.findAll().stream()
                .map(Pago::getNumeroComprobante)
                .mapToInt(numero -> Integer.parseInt(numero.replace("CPR-", "")))
                .max()
                .orElse(0);
        return String.format(Locale.ROOT, "CPR-%06d", ultimo + 1);
    }

    private boolean coincide(Pago pago, String buscar) {
        String texto = buscar.trim().toLowerCase(Locale.ROOT);
        return Stream.of(pago.getNumeroComprobante(), pago.getFolio(), pago.getEmpresa(), pago.getConcepto())
                .anyMatch(campo -> campo != null && campo.toLowerCase(Locale.ROOT).contains(texto));
    }

    private PagoResponse aRespuesta(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getNumeroComprobante(),
                pago.getCompromisoId(),
                pago.getUsuarioId(),
                pago.getNombreUsuario(),
                pago.getRutUsuario(),
                pago.getFolio(),
                pago.getEmpresa(),
                pago.getRazonSocial(),
                pago.getConcepto(),
                pago.getNumeroCuota(),
                pago.getTotalCuotas(),
                pago.getMontoCuota(),
                pago.getVencimiento(),
                pago.getFechaHora(),
                pago.getMontoPagado(),
                pago.getCanal().name(),
                pago.getCanal().getEtiqueta(),
                pago.getMedioPago(),
                pago.getATiempo()
        );
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
