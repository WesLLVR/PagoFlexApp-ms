package cl.duocuc.pagos.client;

import cl.duocuc.pagos.dto.CambiarEstadoDto;
import cl.duocuc.pagos.dto.CompromisoDto;
import cl.duocuc.pagos.dto.ErrorDto;
import cl.duocuc.pagos.dto.PagableDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

@Component
public class CompromisoClient {

    private final WebClient webClient;

    public CompromisoClient(WebClient.Builder builder, @Value("${compromisos.service.url}") String compromisosUrl) {
        this.webClient = builder.baseUrl(compromisosUrl).build();
    }

    public CompromisoDto obtener(Long id) {
        return llamar(() -> webClient.get()
                .uri("/api/compromisos/{id}", id)
                .retrieve()
                .bodyToMono(CompromisoDto.class)
                .block());
    }

    public PagableDto pagable(Long id) {
        return llamar(() -> webClient.get()
                .uri("/api/compromisos/{id}/pagable", id)
                .retrieve()
                .bodyToMono(PagableDto.class)
                .block());
    }

    public List<CompromisoDto> cuotas(Long id) {
        return llamar(() -> webClient.get()
                .uri("/api/compromisos/{id}/cuotas", id)
                .retrieve()
                .bodyToFlux(CompromisoDto.class)
                .collectList()
                .block());
    }

    public List<CompromisoDto> considerados(Long usuarioId) {
        return llamar(() -> webClient.get()
                .uri("/api/compromisos/considerados?usuarioId={id}", usuarioId)
                .retrieve()
                .bodyToFlux(CompromisoDto.class)
                .collectList()
                .block());
    }

    public void marcarPagado(Long id) {
        llamar(() -> webClient.patch()
                .uri("/api/compromisos/{id}/estado", id)
                .bodyValue(new CambiarEstadoDto("PAGADO"))
                .retrieve()
                .toBodilessEntity()
                .block());
    }

    private <T> T llamar(Supplier<T> peticion) {
        try {
            return peticion.get();
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(e.getStatusCode(), mensaje(e));
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de compromisos no está disponible");
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
}
