package cl.duocuc.compromisos.client;

import cl.duocuc.compromisos.dto.CrearPersonaDto;
import cl.duocuc.compromisos.dto.ErrorDto;
import cl.duocuc.compromisos.dto.UsuarioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class UsuarioClient {

    private final WebClient webClient;

    public UsuarioClient(WebClient.Builder builder, @Value("${usuarios.service.url}") String usuariosUrl) {
        this.webClient = builder.baseUrl(usuariosUrl).build();
    }

    public UsuarioDto buscarPorRut(String rut) {
        try {
            return webClient.get()
                    .uri("/api/usuarios/rut/{rut}", rut)
                    .retrieve()
                    .bodyToMono(UsuarioDto.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    public UsuarioDto crear(CrearPersonaDto datos) {
        try {
            return webClient.post()
                    .uri("/api/usuarios")
                    .bodyValue(datos)
                    .retrieve()
                    .bodyToMono(UsuarioDto.class)
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
            return error != null && error.message() != null ? error.message() : "No se pudo registrar a la persona";
        } catch (RuntimeException ex) {
            return "No se pudo registrar a la persona";
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible");
    }
}
