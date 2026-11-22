package cl.duocuc.compromisos.service;

import cl.duocuc.compromisos.client.UsuarioClient;
import cl.duocuc.compromisos.dto.AnularRequest;
import cl.duocuc.compromisos.dto.CompromisoResponse;
import cl.duocuc.compromisos.dto.CrearPersonaDto;
import cl.duocuc.compromisos.dto.PagableResponse;
import cl.duocuc.compromisos.dto.PaginaResponse;
import cl.duocuc.compromisos.dto.RegistroCompromisoRequest;
import cl.duocuc.compromisos.dto.ResumenCobranzaResponse;
import cl.duocuc.compromisos.dto.ResumenMesResponse;
import cl.duocuc.compromisos.dto.UsuarioDto;
import cl.duocuc.compromisos.model.Compromiso;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.model.EstadoCompromiso;
import cl.duocuc.compromisos.model.MotivoAnulacion;
import cl.duocuc.compromisos.repository.CompromisoRepository;
import cl.duocuc.compromisos.util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CompromisoService {

    private static final String VENCIDO = "VENCIDO";
    private static final String VIGENTE = "VIGENTE";
    private static final int MONTO_MAXIMO = 50_000_000;
    private static final Set<String> ORDEN_ASCENDENTE = Set.of(VIGENTE, "PENDIENTE", VENCIDO, "EN_REVISION");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final CompromisoRepository compromisoRepository;
    private final EmpresaService empresaService;
    private final UsuarioClient usuarioClient;

    public PaginaResponse<CompromisoResponse> listar(Long usuarioId, Long empresaId, String estado, String buscar,
                                                     String mes, int pagina, int tamano) {
        YearMonth periodo = vacio(mes) ? null : YearMonth.parse(mes);
        String filtro = vacio(estado) ? null : estado.toUpperCase(Locale.ROOT);

        Comparator<Compromiso> orden = Comparator.comparing(Compromiso::getVencimiento);
        if (filtro == null || !ORDEN_ASCENDENTE.contains(filtro)) {
            orden = orden.reversed();
        }

        List<CompromisoResponse> resultado = base(usuarioId, empresaId)
                .filter(c -> empresaId == null || c.getEmpresa().getId().equals(empresaId))
                .filter(c -> filtro == null || cumpleEstado(c, filtro))
                .filter(c -> periodo == null || YearMonth.from(c.getVencimiento()).equals(periodo))
                .filter(c -> vacio(buscar) || coincide(c, buscar))
                .sorted(orden)
                .map(this::aRespuesta)
                .toList();
        return Paginacion.paginar(resultado, pagina, tamano);
    }

    public CompromisoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public List<CompromisoResponse> cuotas(Long id) {
        Compromiso compromiso = buscar(id);
        return compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(
                        compromiso.getUsuarioId(), compromiso.getEmpresa().getId(), compromiso.getDeuda())
                .stream()
                .sorted(Comparator.comparing(Compromiso::getVencimiento))
                .map(this::aRespuesta)
                .toList();
    }

    public PagableResponse pagable(Long id) {
        Compromiso compromiso = buscar(id);
        String motivo = switch (compromiso.getEstado()) {
            case PAGADO -> "Este compromiso ya está pagado.";
            case ANULADO -> "La empresa anuló este compromiso, así que no corresponde pagarlo.";
            case EN_REVISION -> "Estamos revisando tu reporte. Podrás pagarlo cuando se resuelva.";
            case PENDIENTE -> motivoBloqueo(compromiso);
        };
        return new PagableResponse(motivo == null, motivo);
    }

    public ResumenMesResponse resumenMes(Long usuarioId) {
        LocalDate finDeMes = YearMonth.from(hoy()).atEndOfMonth();
        List<Compromiso> compromisos = compromisoRepository.findByUsuarioId(usuarioId);

        int totalMes = compromisos.stream()
                .filter(c -> c.getEstado() == EstadoCompromiso.PENDIENTE && !c.getVencimiento().isAfter(finDeMes))
                .mapToInt(this::totalAPagar)
                .sum();
        Compromiso proximo = compromisos.stream()
                .filter(c -> estadoActual(c).equals("PENDIENTE"))
                .min(Comparator.comparing(Compromiso::getVencimiento))
                .orElse(null);
        int vencidos = (int) compromisos.stream().filter(c -> estadoActual(c).equals(VENCIDO)).count();

        return new ResumenMesResponse(
                totalMes,
                proximo == null ? null : proximo.getVencimiento(),
                proximo == null ? null : proximo.getEmpresa().getNombre(),
                vencidos
        );
    }

    public List<CompromisoResponse> proximos(Long usuarioId, int dias) {
        return compromisoRepository.findByUsuarioId(usuarioId).stream()
                .filter(c -> c.getEstado() == EstadoCompromiso.PENDIENTE)
                .filter(c -> ChronoUnit.DAYS.between(hoy(), c.getVencimiento()) <= dias)
                .sorted(Comparator.comparing(Compromiso::getVencimiento))
                .map(this::aRespuesta)
                .toList();
    }

    public List<CompromisoResponse> consideradosCumplimiento(Long usuarioId) {
        LocalDate desde = hoy().minusMonths(12);
        return compromisoRepository.findByUsuarioId(usuarioId).stream()
                .filter(c -> !c.getVencimiento().isBefore(desde) && c.getVencimiento().isBefore(hoy()))
                .filter(c -> c.getEstado() != EstadoCompromiso.ANULADO && c.getEstado() != EstadoCompromiso.EN_REVISION)
                .map(this::aRespuesta)
                .toList();
    }

    public ResumenCobranzaResponse resumenCobranza(Long empresaId, String mes) {
        YearMonth periodo = vacio(mes) ? YearMonth.from(hoy()) : YearMonth.parse(mes);
        List<Compromiso> delMes = compromisoRepository.findByEmpresaId(empresaId).stream()
                .filter(c -> YearMonth.from(c.getVencimiento()).equals(periodo))
                .toList();

        int pagados = contar(delMes, "PAGADO");
        int pendientes = contar(delMes, "PENDIENTE");
        int vencidos = contar(delMes, VENCIDO);
        int enRevision = contar(delMes, "EN_REVISION");
        int anulados = contar(delMes, "ANULADO");
        long recaudado = delMes.stream()
                .filter(c -> c.getEstado() == EstadoCompromiso.PAGADO)
                .mapToLong(Compromiso::getMonto)
                .sum();
        int considerados = pagados + pendientes + vencidos + enRevision;
        Double tasa = considerados == 0 ? null : pagados * 100.0 / considerados;

        return new ResumenCobranzaResponse(periodo.toString(), pagados, pendientes, vencidos, enRevision, anulados, recaudado, tasa);
    }

    public CompromisoResponse registrar(RegistroCompromisoRequest datos) {
        Empresa empresa = empresaService.buscar(datos.empresaId());
        validarRegistro(datos);

        String rut = datos.rut().trim().toUpperCase(Locale.ROOT);
        UsuarioDto persona = usuarioClient.buscarPorRut(rut);
        String concepto = datos.concepto().trim();

        if (persona == null) {
            if (vacio(datos.nombre()) || vacio(datos.correo()) || vacio(datos.telefono())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La persona no está registrada en PagoFlex. Ingresa su nombre, correo y teléfono");
            }
            persona = usuarioClient.crear(new CrearPersonaDto(datos.nombre(), rut, datos.correo(), datos.telefono()));
        } else if (existeDuplicado(persona.id(), empresa.getId(), concepto, datos.numeroCuota())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe este compromiso para la persona (misma empresa, concepto y cuota)");
        }

        Compromiso nuevo = Compromiso.builder()
                .folio(siguienteFolio())
                .usuarioId(persona.id())
                .nombreUsuario(persona.nombre())
                .rutUsuario(persona.rut())
                .empresa(empresa)
                .deuda(concepto)
                .concepto(concepto)
                .numeroCuota(datos.numeroCuota())
                .totalCuotas(datos.totalCuotas())
                .monto(datos.monto())
                .vencimiento(datos.vencimiento())
                .estado(EstadoCompromiso.PENDIENTE)
                .ejecutivoRegistroId(datos.ejecutivoId())
                .fechaRegistro(hoy())
                .build();
        return aRespuesta(compromisoRepository.save(nuevo));
    }

    public CompromisoResponse cambiarEstado(Long id, String estado) {
        Compromiso compromiso = buscar(id);
        EstadoCompromiso nuevo = convertir(estado);
        EstadoCompromiso actual = compromiso.getEstado();

        boolean permitido = switch (nuevo) {
            case PAGADO, EN_REVISION -> actual == EstadoCompromiso.PENDIENTE;
            case PENDIENTE -> actual == EstadoCompromiso.EN_REVISION;
            case ANULADO -> false;
        };
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar el compromiso de " + actual + " a " + nuevo);
        }

        compromiso.setEstado(nuevo);
        return aRespuesta(compromisoRepository.save(compromiso));
    }

    public CompromisoResponse anular(Long id, AnularRequest datos) {
        Compromiso compromiso = buscar(id);
        if (vacio(datos.motivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo es obligatorio");
        }
        MotivoAnulacion motivo;
        try {
            motivo = MotivoAnulacion.valueOf(datos.motivo().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo no es válido");
        }
        String comentario = datos.comentario() == null ? "" : datos.comentario().trim();
        if (motivo == MotivoAnulacion.OTRO && comentario.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Si eliges \"Otro\", explica el motivo (mínimo 10 caracteres)");
        }
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO || compromiso.getEstado() == EstadoCompromiso.ANULADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este compromiso ya no se puede anular");
        }

        compromiso.setEstado(EstadoCompromiso.ANULADO);
        compromiso.setMotivoAnulacion(motivo);
        compromiso.setComentarioAnulacion(comentario);
        compromiso.setEjecutivoAnulacionId(datos.ejecutivoId());
        compromiso.setFechaAnulacion(hoy());
        return aRespuesta(compromisoRepository.save(compromiso));
    }

    public void eliminar(Long id) {
        Compromiso compromiso = buscar(id);
        if (compromiso.getEstado() != EstadoCompromiso.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede eliminar un compromiso pendiente");
        }
        compromisoRepository.delete(compromiso);
    }

    public String estadoActual(Compromiso compromiso) {
        if (compromiso.getEstado() == EstadoCompromiso.PENDIENTE && compromiso.getVencimiento().isBefore(hoy())) {
            return VENCIDO;
        }
        return compromiso.getEstado().name();
    }

    private Stream<Compromiso> base(Long usuarioId, Long empresaId) {
        if (usuarioId != null) {
            return compromisoRepository.findByUsuarioId(usuarioId).stream();
        }
        if (empresaId != null) {
            return compromisoRepository.findByEmpresaId(empresaId).stream();
        }
        return compromisoRepository.findAll().stream();
    }

    private boolean cumpleEstado(Compromiso compromiso, String filtro) {
        String estado = estadoActual(compromiso);
        if (filtro.equals(VIGENTE)) {
            return !estado.equals("PAGADO") && !estado.equals("ANULADO");
        }
        return estado.equals(filtro);
    }

    private boolean coincide(Compromiso compromiso, String buscar) {
        String texto = buscar.trim().toLowerCase(Locale.ROOT);
        return Stream.of(compromiso.getFolio(), compromiso.getConcepto(), compromiso.getNombreUsuario(),
                        compromiso.getRutUsuario(), compromiso.getEmpresa().getNombre())
                .anyMatch(campo -> campo != null && campo.toLowerCase(Locale.ROOT).contains(texto));
    }

    private String motivoBloqueo(Compromiso compromiso) {
        return compromisoRepository.findByUsuarioIdAndEmpresaIdAndDeudaIgnoreCaseOrderByNumeroCuotaAsc(
                        compromiso.getUsuarioId(), compromiso.getEmpresa().getId(), compromiso.getDeuda())
                .stream()
                .filter(otro -> !otro.getId().equals(compromiso.getId()))
                .filter(otro -> otro.getEstado() == EstadoCompromiso.PENDIENTE)
                .filter(otro -> otro.getVencimiento().isBefore(compromiso.getVencimiento()))
                .min(Comparator.comparing(Compromiso::getVencimiento))
                .map(anterior -> "Primero paga " + etiqueta(anterior) + " de " + anterior.getEmpresa().getNombre()
                        + ", que vence el " + anterior.getVencimiento().format(FORMATO_FECHA) + ".")
                .orElse(null);
    }

    private String etiqueta(Compromiso compromiso) {
        if (compromiso.getNumeroCuota() != null && compromiso.getTotalCuotas() != null) {
            return "la cuota " + compromiso.getNumeroCuota() + "/" + compromiso.getTotalCuotas();
        }
        return "\"" + compromiso.getConcepto() + "\"";
    }

    private void validarRegistro(RegistroCompromisoRequest datos) {
        if (vacio(datos.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUT es obligatorio");
        }
        if (vacio(datos.concepto()) || datos.concepto().trim().length() < 3 || datos.concepto().trim().length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El concepto debe tener entre 3 y 60 caracteres");
        }
        if (datos.monto() == null || datos.monto() <= 0 || datos.monto() > MONTO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor que cero y menor a 50.000.000");
        }
        if (datos.vencimiento() == null || datos.vencimiento().isBefore(hoy())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser anterior a hoy");
        }
        Integer cuota = datos.numeroCuota();
        Integer total = datos.totalCuotas();
        if ((cuota == null) != (total == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica el número de cuota y el total de cuotas");
        }
        if (cuota != null && (cuota <= 0 || total <= 0 || cuota > total)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuota debe ser mayor que cero y no superar el total");
        }
    }

    private boolean existeDuplicado(Long usuarioId, Long empresaId, String concepto, Integer numeroCuota) {
        return compromisoRepository.findByUsuarioIdAndEmpresaIdAndConceptoIgnoreCase(usuarioId, empresaId, concepto)
                .stream()
                .filter(c -> c.getEstado() != EstadoCompromiso.ANULADO)
                .anyMatch(c -> numeroCuota == null ? c.getNumeroCuota() == null : numeroCuota.equals(c.getNumeroCuota()));
    }

    private String siguienteFolio() {
        long siguiente = compromisoRepository.findTopByOrderByIdDesc().map(Compromiso::getId).orElse(0L) + 1;
        return String.format(Locale.ROOT, "CP-%d-%04d", hoy().getYear(), 300 + siguiente);
    }

    private EstadoCompromiso convertir(String estado) {
        try {
            return EstadoCompromiso.valueOf(estado.toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado no es válido");
        }
    }

    private int contar(List<Compromiso> compromisos, String estado) {
        return (int) compromisos.stream().filter(c -> estadoActual(c).equals(estado)).count();
    }

    private int recargo(Compromiso compromiso) {
        return estadoActual(compromiso).equals(VENCIDO) ? compromiso.getEmpresa().getRecargo() : 0;
    }

    private int totalAPagar(Compromiso compromiso) {
        return compromiso.getMonto() + recargo(compromiso);
    }

    private Compromiso buscar(Long id) {
        return compromisoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compromiso no encontrado"));
    }

    private CompromisoResponse aRespuesta(Compromiso c) {
        String estado = estadoActual(c);
        long diasAtraso = estado.equals(VENCIDO) ? ChronoUnit.DAYS.between(c.getVencimiento(), hoy()) : 0;
        return new CompromisoResponse(
                c.getId(),
                c.getFolio(),
                c.getUsuarioId(),
                c.getNombreUsuario(),
                c.getRutUsuario(),
                c.getEmpresa().getId(),
                c.getEmpresa().getNombre(),
                c.getEmpresa().getRazonSocial(),
                c.getDeuda(),
                c.getConcepto(),
                c.getNumeroCuota(),
                c.getTotalCuotas(),
                c.getMonto(),
                recargo(c),
                totalAPagar(c),
                c.getVencimiento(),
                estado,
                diasAtraso,
                c.getMotivoAnulacion() == null ? null : c.getMotivoAnulacion().getEtiqueta(),
                c.getComentarioAnulacion()
        );
    }

    private LocalDate hoy() {
        return LocalDate.now();
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
