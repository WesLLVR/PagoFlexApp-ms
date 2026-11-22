package cl.duocuc.compromisos.service;

import cl.duocuc.compromisos.dto.EmpresaResponse;
import cl.duocuc.compromisos.model.Empresa;
import cl.duocuc.compromisos.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<EmpresaResponse> listar() {
        return empresaRepository.findAllByOrderByNombreAsc().stream().map(this::aRespuesta).toList();
    }

    public EmpresaResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public Empresa buscar(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
    }

    private EmpresaResponse aRespuesta(Empresa empresa) {
        return new EmpresaResponse(empresa.getId(), empresa.getCodigo(), empresa.getNombre(), empresa.getRazonSocial(),
                empresa.getRut(), empresa.getRubro(), empresa.getCorreo(), empresa.getTelefono(), empresa.getRecargo());
    }
}
