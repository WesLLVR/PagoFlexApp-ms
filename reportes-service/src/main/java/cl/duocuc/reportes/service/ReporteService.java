package cl.duocuc.reportes.service;

import cl.duocuc.reportes.client.CompromisoClient;
import cl.duocuc.reportes.client.UsuarioClient;
import cl.duocuc.reportes.dto.CompromisoDto;
import cl.duocuc.reportes.dto.CrearReporteRequest;
import cl.duocuc.reportes.dto.HistorialResponse;
import cl.duocuc.reportes.dto.PaginaResponse;
import cl.duocuc.reportes.dto.ReporteResponse;
import cl.duocuc.reportes.dto.ResumenReportesResponse;
import cl.duocuc.reportes.dto.UsuarioDto;
import cl.duocuc.reportes.model.EstadoReporte;
import cl.duocuc.reportes.model.HistorialEstado;
import cl.duocuc.reportes.model.MotivoReporte;
import cl.duocuc.reportes.model.Reporte;
import cl.duocuc.reportes.repository.HistorialEstadoRepository;
import cl.duocuc.reportes.repository.ReporteRepository;
import cl.duocuc.reportes.util.DiasHabiles;
import cl.duocuc.reportes.util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final int LARGO_MINIMO = 10;
    private static final int LARGO_MAXIMO = 300;
    private static final int DIAS_HABILES_PLAZO = 2;

    private static final Map<EstadoReporte, Set<EstadoReporte>> TRANSICIONES = Map.of(
            EstadoReporte.ABIERTO, Set.of(EstadoReporte.EN_GESTION),
            EstadoReporte.EN_GESTION, Set.of(EstadoReporte.RESUELTO),
            EstadoReporte.RESUELTO, Set.of(EstadoReporte.EN_GESTION, EstadoReporte.CERRADO),
            EstadoReporte.CERRADO, Set.of()
    );

    private final ReporteRepository reporteRepository;
    private final HistorialEstadoRepository historialRepository;
    private final CompromisoClient compromisoClient;
    private final UsuarioClient usuarioClient;
    private final AlmacenamientoService almacenamientoService;

    public ReporteResponse crear(CrearReporteRequest datos) {
        MotivoReporte motivo = convertirMotivo(datos.motivo());
        String comentario = datos.comentario() == null ? "" : datos.comentario().trim();
        if (comentario.length() > LARGO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 300 caracteres");
        }
        if (motivo == MotivoReporte.OTRO && comentario.length() < LARGO_MINIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Si eliges \"Otro\", cuéntanos el problema (mínimo 10 caracteres)");
        }

        CompromisoDto compromiso = compromisoClient.obtener(datos.compromisoId());
        if (!compromiso.usuarioId().equals(datos.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este compromiso no es tuyo");
        }
        if (!compromiso.estado().equals("PENDIENTE") && !compromiso.estado().equals("VENCIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo puedes reportar compromisos pendientes o vencidos");
        }

        LocalDateTime ahora = ahora();
        Reporte reporte = reporteRepository.save(Reporte.builder()
                .numero(siguienteNumero())
                .compromisoId(compromiso.id())
                .folio(compromiso.folio())
                .concepto(compromiso.concepto())
                .empresa(compromiso.empresa())
                .usuarioId(compromiso.usuarioId())
                .nombreUsuario(compromiso.nombreUsuario())
                .motivo(motivo)
                .comentario(comentario)
                .estado(EstadoReporte.ABIERTO)
                .fechaCreacion(ahora)
                .build());

        try {
            compromisoClient.cambiarEstado(compromiso.id(), "EN_REVISION");
        } catch (ResponseStatusException e) {
            reporteRepository.delete(reporte);
            throw e;
        }

        registrar(reporte.getId(), null, EstadoReporte.ABIERTO, reporte.getNombreUsuario(), comentario, ahora);
        return aRespuesta(reporte);
    }

    public PaginaResponse<ReporteResponse> listar(String estado, Long usuarioId, Long agenteId, String buscar,
                                                  boolean soloAtrasados, int pagina, int tamano) {
        Stream<Reporte> base = usuarioId == null
                ? reporteRepository.findAll().stream()
                : reporteRepository.findByUsuarioId(usuarioId).stream();

        List<ReporteResponse> resultado = base
                .filter(r -> vacio(estado) || r.getEstado().name().equalsIgnoreCase(estado))
                .filter(r -> agenteId == null || agenteId.equals(r.getAgenteId()))
                .filter(r -> vacio(buscar) || coincide(r, buscar))
                .filter(r -> !soloAtrasados || atrasado(r))
                .sorted(Comparator.comparing((Reporte r) -> !atrasado(r))
                        .thenComparing(Reporte::getEstado)
                        .thenComparing(Reporte::getFechaCreacion, Comparator.reverseOrder()))
                .map(this::aRespuestaSinHistorial)
                .toList();
        return Paginacion.paginar(resultado, pagina, tamano);
    }

    public ResumenReportesResponse resumen() {
        List<Reporte> todos = reporteRepository.findAll();
        return new ResumenReportesResponse(
                contar(todos, EstadoReporte.ABIERTO),
                contar(todos, EstadoReporte.EN_GESTION),
                contar(todos, EstadoReporte.RESUELTO),
                contar(todos, EstadoReporte.CERRADO),
                (int) todos.stream().filter(this::atrasado).count()
        );
    }

    public ReporteResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public ReporteResponse ultimoDeCompromiso(Long compromisoId) {
        return reporteRepository.findTopByCompromisoIdOrderByIdDesc(compromisoId)
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Este compromiso no tiene reportes"));
    }

    public ReporteResponse tomar(Long id, Long agenteId) {
        Reporte reporte = buscar(id);
        UsuarioDto agente = buscarAgente(agenteId);
        cambiar(reporte, EstadoReporte.EN_GESTION);

        reporte.setAgenteId(agente.id());
        reporte.setNombreAgente(agente.nombre());
        reporte.setFechaToma(ahora());
        reporteRepository.save(reporte);
        registrar(id, EstadoReporte.ABIERTO, EstadoReporte.EN_GESTION, agente.nombre(), null, reporte.getFechaToma());
        return aRespuesta(reporte);
    }

    public ReporteResponse resolver(Long id, Long agenteId, String respuesta) {
        Reporte reporte = buscar(id);
        if (!agenteId.equals(reporte.getAgenteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este reporte lo está gestionando otro agente");
        }
        String texto = respuesta == null ? "" : respuesta.trim();
        if (texto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe la respuesta para el usuario antes de resolver");
        }
        if (texto.length() < LARGO_MINIMO || texto.length() > LARGO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La respuesta debe tener entre 10 y 300 caracteres");
        }
        cambiar(reporte, EstadoReporte.RESUELTO);

        reporte.setRespuesta(texto);
        reporte.setFechaResolucion(ahora());
        reporteRepository.save(reporte);
        registrar(id, EstadoReporte.EN_GESTION, EstadoReporte.RESUELTO, reporte.getNombreAgente(), texto,
                reporte.getFechaResolucion());
        return aRespuesta(reporte);
    }

    public ReporteResponse reabrir(Long id, Long usuarioId, String comentario) {
        Reporte reporte = buscarDelUsuario(id, usuarioId);
        String texto = comentario == null ? "" : comentario.trim();
        if (texto.length() < LARGO_MINIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cuéntanos por qué no quedó resuelto (mínimo 10 caracteres)");
        }
        cambiar(reporte, EstadoReporte.EN_GESTION);

        reporte.setFechaResolucion(null);
        reporteRepository.save(reporte);
        registrar(id, EstadoReporte.RESUELTO, EstadoReporte.EN_GESTION, reporte.getNombreUsuario(), texto, ahora());
        return aRespuesta(reporte);
    }

    public ReporteResponse cerrar(Long id, Long usuarioId) {
        Reporte reporte = buscarDelUsuario(id, usuarioId);
        cambiar(reporte, EstadoReporte.CERRADO);

        reporte.setFechaCierre(ahora());
        reporteRepository.save(reporte);
        registrar(id, EstadoReporte.RESUELTO, EstadoReporte.CERRADO, reporte.getNombreUsuario(), null,
                reporte.getFechaCierre());

        try {
            compromisoClient.cambiarEstado(reporte.getCompromisoId(), "PENDIENTE");
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() != HttpStatus.CONFLICT) {
                throw e;
            }
        }
        return aRespuesta(reporte);
    }

    public ReporteResponse subirFoto(Long id, MultipartFile archivo) {
        Reporte reporte = buscar(id);
        String anterior = reporte.getFoto();
        reporte.setFoto(almacenamientoService.guardar(archivo));
        reporteRepository.save(reporte);
        almacenamientoService.eliminar(anterior);
        return aRespuesta(reporte);
    }

    public byte[] obtenerFoto(Long id) {
        Reporte reporte = buscar(id);
        if (reporte.getFoto() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El reporte no tiene foto");
        }
        return almacenamientoService.leer(reporte.getFoto());
    }

    public ReporteResponse eliminarFoto(Long id) {
        Reporte reporte = buscar(id);
        String anterior = reporte.getFoto();
        reporte.setFoto(null);
        reporteRepository.save(reporte);
        almacenamientoService.eliminar(anterior);
        return aRespuesta(reporte);
    }

    public boolean atrasado(Reporte reporte) {
        boolean abierto = reporte.getEstado() == EstadoReporte.ABIERTO || reporte.getEstado() == EstadoReporte.EN_GESTION;
        return abierto && DiasHabiles.entre(reporte.getFechaCreacion().toLocalDate(), LocalDate.now()) > DIAS_HABILES_PLAZO;
    }

    private void cambiar(Reporte reporte, EstadoReporte nuevo) {
        if (!TRANSICIONES.get(reporte.getEstado()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un reporte " + reporte.getEstado().getEtiqueta().toLowerCase(Locale.ROOT)
                            + " no puede pasar a " + nuevo.getEtiqueta().toLowerCase(Locale.ROOT));
        }
        reporte.setEstado(nuevo);
    }

    private void registrar(Long reporteId, EstadoReporte anterior, EstadoReporte nuevo, String autor, String comentario,
                           LocalDateTime fecha) {
        historialRepository.save(HistorialEstado.builder()
                .reporteId(reporteId)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fecha(fecha)
                .autor(autor)
                .comentario(comentario)
                .build());
    }

    private UsuarioDto buscarAgente(Long agenteId) {
        UsuarioDto agente = agenteId == null ? null : usuarioClient.obtener(agenteId);
        if (agente == null || !"AGENTE".equals(agente.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un agente de PagoFlex puede tomar reportes");
        }
        return agente;
    }

    private Reporte buscarDelUsuario(Long id, Long usuarioId) {
        Reporte reporte = buscar(id);
        if (!reporte.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este reporte no es tuyo");
        }
        return reporte;
    }

    private Reporte buscar(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
    }

    private MotivoReporte convertirMotivo(String motivo) {
        if (vacio(motivo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige el motivo del problema");
        }
        try {
            return MotivoReporte.valueOf(motivo.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo no es válido");
        }
    }

    private String siguienteNumero() {
        int ultimo = reporteRepository.findAll().stream()
                .mapToInt(r -> Integer.parseInt(r.getNumero().replace("REP-", "")))
                .max()
                .orElse(0);
        return String.format(Locale.ROOT, "REP-%04d", ultimo + 1);
    }

    private boolean coincide(Reporte reporte, String buscar) {
        String texto = buscar.trim().toLowerCase(Locale.ROOT);
        return Stream.of(reporte.getNumero(), reporte.getFolio(), reporte.getNombreUsuario(), reporte.getEmpresa(),
                        reporte.getConcepto())
                .anyMatch(campo -> campo != null && campo.toLowerCase(Locale.ROOT).contains(texto));
    }

    private int contar(List<Reporte> reportes, EstadoReporte estado) {
        return (int) reportes.stream().filter(r -> r.getEstado() == estado).count();
    }

    private ReporteResponse aRespuesta(Reporte reporte) {
        List<HistorialResponse> historial = historialRepository.findByReporteIdOrderByFechaAsc(reporte.getId()).stream()
                .map(h -> new HistorialResponse(
                        h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(),
                        h.getEstadoNuevo().name(),
                        h.getEstadoNuevo().getEtiqueta(),
                        h.getFecha(),
                        h.getAutor(),
                        h.getComentario()))
                .toList();
        return construir(reporte, historial);
    }

    private ReporteResponse aRespuestaSinHistorial(Reporte reporte) {
        return construir(reporte, List.of());
    }

    private ReporteResponse construir(Reporte r, List<HistorialResponse> historial) {
        return new ReporteResponse(
                r.getId(), r.getNumero(), r.getCompromisoId(), r.getFolio(), r.getConcepto(), r.getEmpresa(),
                r.getUsuarioId(), r.getNombreUsuario(), r.getMotivo().name(), r.getMotivo().getEtiqueta(),
                r.getComentario(), urlFoto(r), r.getEstado().name(), r.getEstado().getEtiqueta(), r.getAgenteId(),
                r.getNombreAgente(), r.getRespuesta(), r.getFechaCreacion(), r.getFechaToma(), r.getFechaResolucion(),
                r.getFechaCierre(), atrasado(r), historial);
    }

    private String urlFoto(Reporte reporte) {
        if (reporte.getFoto() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reportes/" + reporte.getId() + "/foto")
                .queryParam("v", reporte.getFoto())
                .toUriString();
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
