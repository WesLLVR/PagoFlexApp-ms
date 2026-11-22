package cl.duocuc.reportes.client;

import cl.duocuc.reportes.dto.CambiarEstadoDto;
import cl.duocuc.reportes.dto.CompromisoDto;
import cl.duocuc.reportes.dto.ErrorDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CompromisoClient {

    private final WebClient webClient;

    public CompromisoClient(WebClient.Builder builder, @Value("${compromisos.service.url}") String compromisosUrl) {
        this.webClient = builder.baseUrl(compromisosUrl).build();
    }

    public CompromisoDto obtener(Long id) {
        try {
            return webClient.get()
                    .uri("/api/compromisos/{id}", id)
                    .retrieve()
                    .bodyToMono(CompromisoDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(e.getStatusCode(), mensaje(e));
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    public void cambiarEstado(Long id, String estado) {
        try {
            webClient.patch()
                    .uri("/api/compromisos/{id}/estado", id)
                    .bodyValue(new CambiarEstadoDto(estado))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(e.getStatusCode(), mensaje(e));
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    private String mensaje(WebClientResponseException e) {
        try {
            ErrorDto error = e.getResponseBodyAs(ErrorDto.class);
            return error != null && error.message() != null ? error.message() : "Error en el servicio de compromisos";
        } catch (RuntimeException ex) {
            return "Error en el servicio de compromisos";
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de compromisos no está disponible");
    }
}
