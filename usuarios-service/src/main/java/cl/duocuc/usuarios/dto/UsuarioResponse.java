package cl.duocuc.usuarios.dto;

public record UsuarioResponse(
        Long id,
        String nombre,
        String rut,
        String correo,
        String telefono,
        String comuna,
        String rol,
        String rolNombre,
        Long empresaId,
        String cargo,
        String avatarUrl
) {
}
