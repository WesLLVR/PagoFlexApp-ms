package cl.duocuc.reportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompromisoDto(Long id, String folio, Long usuarioId, String nombreUsuario, String empresa,
                            String concepto, String estado) {
}
