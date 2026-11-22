package cl.duocuc.pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PagableDto(boolean pagable, String motivo) {
}
