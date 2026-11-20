package cl.duocuc.usuarios.dto;

public record RestablecerRequest(String correo, String codigo, String nueva) {
}
