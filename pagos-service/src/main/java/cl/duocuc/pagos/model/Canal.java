package cl.duocuc.pagos.model;

public enum Canal {
    APP("App"),
    PORTAL("Portal de pagos"),
    AGENTE("Agente conversacional");

    private final String etiqueta;

    Canal(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
