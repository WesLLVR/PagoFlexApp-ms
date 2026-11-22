package cl.duocuc.compromisos.controller;

import cl.duocuc.compromisos.dto.AnularRequest;
import cl.duocuc.compromisos.dto.CambiarEstadoRequest;
import cl.duocuc.compromisos.dto.CompromisoResponse;
import cl.duocuc.compromisos.dto.PagableResponse;
import cl.duocuc.compromisos.dto.PaginaResponse;
import cl.duocuc.compromisos.dto.RegistroCompromisoRequest;
import cl.duocuc.compromisos.dto.ResumenCobranzaResponse;
import cl.duocuc.compromisos.dto.ResumenMesResponse;
import cl.duocuc.compromisos.service.CompromisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@Tag(name = "Compromisos", description = "Compromisos de pago y sus cuotas")
@RestController
@RequestMapping("/api/compromisos")
@RequiredArgsConstructor
public class CompromisoController {

    private final CompromisoService compromisoService;

    @Operation(summary = "Lista compromisos con filtros por usuario, empresa, estado, mes y búsqueda, con paginación")
    @GetMapping
    public PaginaResponse<CompromisoResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String mes,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamano) {
        return compromisoService.listar(usuarioId, empresaId, estado, buscar, mes, pagina, tamano);
    }

    @Operation(summary = "Obtiene un compromiso con su recargo y total a pagar")
    @GetMapping("/{id}")
    public CompromisoResponse obtener(@PathVariable Long id) {
        return compromisoService.obtener(id);
    }

    @Operation(summary = "Lista todas las cuotas de la misma deuda")
    @GetMapping("/{id}/cuotas")
    public List<CompromisoResponse> cuotas(@PathVariable Long id) {
        return compromisoService.cuotas(id);
    }

    @Operation(summary = "Indica si se puede pagar o qué cuota anterior falta")
    @GetMapping("/{id}/pagable")
    public PagableResponse pagable(@PathVariable Long id) {
        return compromisoService.pagable(id);
    }

    @Operation(summary = "Resumen del mes del usuario")
    @GetMapping("/resumen-mes")
    public ResumenMesResponse resumenMes(@RequestParam Long usuarioId) {
        return compromisoService.resumenMes(usuarioId);
    }

    @Operation(summary = "Compromisos por vencer y vencidos para los avisos")
    @GetMapping("/proximos")
    public List<CompromisoResponse> proximos(@RequestParam Long usuarioId, @RequestParam(defaultValue = "3") int dias) {
        return compromisoService.proximos(usuarioId, dias);
    }

    @Operation(summary = "Compromisos de los últimos 12 meses para el cumplimiento")
    @GetMapping("/considerados")
    public List<CompromisoResponse> consideradosCumplimiento(@RequestParam Long usuarioId) {
        return compromisoService.consideradosCumplimiento(usuarioId);
    }

    @Operation(summary = "Resumen de cobranza mensual de una empresa")
    @GetMapping("/resumen-cobranza")
    public ResumenCobranzaResponse resumenCobranza(@RequestParam Long empresaId, @RequestParam(required = false) String mes) {
        return compromisoService.resumenCobranza(empresaId, mes);
    }

    @Operation(summary = "Registra un compromiso nuevo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompromisoResponse registrar(@RequestBody RegistroCompromisoRequest datos) {
        return compromisoService.registrar(datos);
    }

    @Operation(summary = "Cambia el estado (lo usan pagos-service y reportes-service)")
    @PatchMapping("/{id}/estado")
    public CompromisoResponse cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest datos) {
        return compromisoService.cambiarEstado(id, datos.estado());
    }

    @Operation(summary = "Anula un compromiso con su motivo")
    @PatchMapping("/{id}/anular")
    public CompromisoResponse anular(@PathVariable Long id, @RequestBody AnularRequest datos) {
        return compromisoService.anular(id, datos);
    }

    @Operation(summary = "Elimina un compromiso pendiente")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        compromisoService.eliminar(id);
    }
}
