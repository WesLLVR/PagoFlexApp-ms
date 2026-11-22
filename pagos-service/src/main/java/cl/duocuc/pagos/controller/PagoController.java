package cl.duocuc.pagos.controller;

import cl.duocuc.pagos.dto.CumplimientoResponse;
import cl.duocuc.pagos.dto.PaginaResponse;
import cl.duocuc.pagos.dto.PagoRealizadoResponse;
import cl.duocuc.pagos.dto.PagoRequest;
import cl.duocuc.pagos.dto.PagoResponse;
import cl.duocuc.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Pagos", description = "Pagos, comprobantes, historial y cumplimiento")
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Paga un compromiso y devuelve la siguiente cuota")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoRealizadoResponse pagar(@RequestBody PagoRequest datos) {
        return pagoService.pagar(datos);
    }

    @Operation(summary = "Historial de pagos con filtros y paginación")
    @GetMapping
    public PaginaResponse<PagoResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String empresa,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamano) {
        return pagoService.listar(usuarioId, empresa, anio, desde, hasta, buscar, pagina, tamano);
    }

    @Operation(summary = "Empresas en las que el usuario tiene pagos")
    @GetMapping("/empresas")
    public List<String> empresas(@RequestParam Long usuarioId) {
        return pagoService.empresas(usuarioId);
    }

    @Operation(summary = "Porcentaje de pagos a tiempo y su categoría")
    @GetMapping("/cumplimiento")
    public CumplimientoResponse cumplimiento(@RequestParam Long usuarioId) {
        return pagoService.cumplimiento(usuarioId);
    }

    @Operation(summary = "Comprobante del pago de un compromiso")
    @GetMapping("/compromiso/{compromisoId}")
    public PagoResponse porCompromiso(@PathVariable Long compromisoId) {
        return pagoService.porCompromiso(compromisoId);
    }

    @Operation(summary = "Obtiene un pago por su id")
    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }
}
