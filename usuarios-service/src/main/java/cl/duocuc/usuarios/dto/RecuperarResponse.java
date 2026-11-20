package cl.duocuc.usuarios.dto;

public record RecuperarResponse(String correo, String codigo, int minutosVigencia) {
}
