package cl.duocuc.compromisos.dto;

public record EmpresaResponse(Long id, String codigo, String nombre, String razonSocial, String rut, String rubro, String correo, String telefono, Integer recargo) {
}
