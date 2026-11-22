package cl.duocuc.reportes.model;

public enum EstadoReporte {
    ABIERTO("Abierto"),
    EN_GESTION("En gestión"),
    RESUELTO("Resuelto"),
    CERRADO("Cerrado");

    private final String etiqueta;

    EstadoReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
