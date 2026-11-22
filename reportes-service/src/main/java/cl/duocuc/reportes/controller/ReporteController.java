package cl.duocuc.reportes.controller;

import cl.duocuc.reportes.dto.CerrarRequest;
import cl.duocuc.reportes.dto.CrearReporteRequest;
import cl.duocuc.reportes.dto.PaginaResponse;
import cl.duocuc.reportes.dto.ReabrirRequest;
import cl.duocuc.reportes.dto.ReporteResponse;
import cl.duocuc.reportes.dto.ResolverRequest;
import cl.duocuc.reportes.dto.ResumenReportesResponse;
import cl.duocuc.reportes.dto.TomarRequest;
import cl.duocuc.reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Reportes", description = "Reportes de problema y su seguimiento")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Crea un reporte y deja el compromiso en revisión")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse crear(@RequestBody CrearReporteRequest datos) {
        return reporteService.crear(datos);
    }

    @Operation(summary = "Lista reportes con filtros y paginación")
    @GetMapping
    public PaginaResponse<ReporteResponse> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long agenteId,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "false") boolean soloAtrasados,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamano) {
        return reporteService.listar(estado, usuarioId, agenteId, buscar, soloAtrasados, pagina, tamano);
    }

    @Operation(summary = "Cantidad de reportes por estado")
    @GetMapping("/resumen")
    public ResumenReportesResponse resumen() {
        return reporteService.resumen();
    }

    @Operation(summary = "Último reporte de un compromiso")
    @GetMapping("/compromiso/{compromisoId}")
    public ReporteResponse ultimoDeCompromiso(@PathVariable Long compromisoId) {
        return reporteService.ultimoDeCompromiso(compromisoId);
    }

    @Operation(summary = "Obtiene un reporte con su historial")
    @GetMapping("/{id}")
    public ReporteResponse obtener(@PathVariable Long id) {
        return reporteService.obtener(id);
    }

    @Operation(summary = "El agente toma el reporte")
    @PatchMapping("/{id}/tomar")
    public ReporteResponse tomar(@PathVariable Long id, @RequestBody TomarRequest datos) {
        return reporteService.tomar(id, datos.agenteId());
    }

    @Operation(summary = "El agente resuelve el reporte con una respuesta")
    @PatchMapping("/{id}/resolver")
    public ReporteResponse resolver(@PathVariable Long id, @RequestBody ResolverRequest datos) {
        return reporteService.resolver(id, datos.agenteId(), datos.respuesta());
    }

    @Operation(summary = "El usuario reabre el reporte resuelto")
    @PatchMapping("/{id}/reabrir")
    public ReporteResponse reabrir(@PathVariable Long id, @RequestBody ReabrirRequest datos) {
        return reporteService.reabrir(id, datos.usuarioId(), datos.comentario());
    }

    @Operation(summary = "El usuario cierra el reporte resuelto")
    @PatchMapping("/{id}/cerrar")
    public ReporteResponse cerrar(@PathVariable Long id, @RequestBody CerrarRequest datos) {
        return reporteService.cerrar(id, datos.usuarioId());
    }

    @Operation(summary = "Sube la foto del comprobante")
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReporteResponse subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return reporteService.subirFoto(id, archivo);
    }

    @Operation(summary = "Descarga la foto del comprobante")
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(reporteService.obtenerFoto(id));
    }

    @Operation(summary = "Quita la foto del comprobante")
    @DeleteMapping("/{id}/foto")
    public ReporteResponse eliminarFoto(@PathVariable Long id) {
        return reporteService.eliminarFoto(id);
    }
}
