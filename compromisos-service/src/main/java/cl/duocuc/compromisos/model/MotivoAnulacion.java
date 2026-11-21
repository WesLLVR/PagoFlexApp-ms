package cl.duocuc.compromisos.model;

public enum MotivoAnulacion {
    REPACTACION("Repactación"),
    ERROR_REGISTRO("Error de registro"),
    PAGADO_DIRECTO("Pagado directamente a la empresa"),
    OTRO("Otro");

    private final String etiqueta;

    MotivoAnulacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
