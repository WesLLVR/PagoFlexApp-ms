package cl.duocuc.usuarios.model;

public enum Rol {
    USUARIO_FINAL("Usuario final"),
    AGENTE("Agente PagoFlex"),
    EJECUTIVO("Ejecutivo de empresa cliente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
