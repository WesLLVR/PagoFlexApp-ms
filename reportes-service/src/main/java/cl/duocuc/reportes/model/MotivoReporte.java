package cl.duocuc.reportes.model;

public enum MotivoReporte {
    YA_PAGUE("Ya pagué por otro medio"),
    NO_RECONOZCO("No reconozco este cobro"),
    MONTO_INCORRECTO("El monto no corresponde"),
    OTRO("Otro");

    private final String etiqueta;

    MotivoReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
