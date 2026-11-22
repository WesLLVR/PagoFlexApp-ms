package cl.duocuc.compromisos.controller;

import cl.duocuc.compromisos.dto.EmpresaResponse;
import cl.duocuc.compromisos.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Empresas", description = "Empresas clientes de PagoFlex")
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @Operation(summary = "Lista las empresas")
    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @Operation(summary = "Obtiene una empresa por su id")
    @GetMapping("/{id}")
    public EmpresaResponse obtener(@PathVariable Long id) {
        return empresaService.obtener(id);
    }
}
