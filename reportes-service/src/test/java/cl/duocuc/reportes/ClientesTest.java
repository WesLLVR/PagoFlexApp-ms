package cl.duocuc.reportes;

import cl.duocuc.reportes.client.CompromisoClient;
import cl.duocuc.reportes.client.UsuarioClient;
import cl.duocuc.reportes.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientesTest {

    private WebClient.Builder responde(HttpStatus estado, String cuerpo) {
        ExchangeFunction respuesta = solicitud -> Mono.just(ClientResponse.create(estado)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(cuerpo)
                .build());
        return WebClient.builder().exchangeFunction(respuesta);
    }

    private WebClient.Builder caido() {
        ExchangeFunction error = solicitud -> Mono.error(new WebClientRequestException(
                new IOException("Conexión rechazada"), HttpMethod.GET, URI.create("http://servicio"), HttpHeaders.EMPTY));
        return WebClient.builder().exchangeFunction(error);
    }

    @Test
    void compromisoClient_obtenerYCambiarEstado() {
        CompromisoClient cliente = new CompromisoClient(responde(HttpStatus.OK,
                "{\"id\":36,\"folio\":\"CP-2026-0106\",\"usuarioId\":2,\"estado\":\"PENDIENTE\"}"), "http://compromisos-service");

        assertEquals("CP-2026-0106", cliente.obtener(36L).folio());
        assertDoesNotThrow(() -> cliente.cambiarEstado(36L, "EN_REVISION"));
    }

    @Test
    void compromisoClient_errores() {
        CompromisoClient conflicto = new CompromisoClient(responde(HttpStatus.CONFLICT,
                "{\"message\":\"No se puede pasar el compromiso\"}"), "http://compromisos-service");
        CompromisoClient sinMensaje = new CompromisoClient(responde(HttpStatus.NOT_FOUND, "texto"), "http://compromisos-service");
        CompromisoClient sinServicio = new CompromisoClient(caido(), "http://compromisos-service");

        assertEquals("No se puede pasar el compromiso",
                assertThrows(ResponseStatusException.class, () -> conflicto.cambiarEstado(36L, "PAGADO")).getReason());
        assertEquals("Error en el servicio de compromisos",
                assertThrows(ResponseStatusException.class, () -> sinMensaje.obtener(99L)).getReason());
        assertEquals("Error en el servicio de compromisos",
                assertThrows(ResponseStatusException.class, () -> new CompromisoClient(responde(HttpStatus.BAD_REQUEST, "{}"),
                        "http://compromisos-service").obtener(1L)).getReason());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> sinServicio.obtener(36L)).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> sinServicio.cambiarEstado(36L, "PAGADO")).getStatusCode());
    }

    @Test
    void usuarioClient_obtenerONullSiNoExiste() {
        UsuarioClient cliente = new UsuarioClient(responde(HttpStatus.OK,
                "{\"id\":9,\"nombre\":\"Andrés Lagos\",\"rol\":\"AGENTE\"}"), "http://usuarios-service");
        UsuarioClient noExiste = new UsuarioClient(responde(HttpStatus.NOT_FOUND, "{}"), "http://usuarios-service");
        UsuarioClient sinServicio = new UsuarioClient(caido(), "http://usuarios-service");

        assertEquals("AGENTE", cliente.obtener(9L).rol());
        assertNull(noExiste.obtener(99L));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> sinServicio.obtener(9L)).getStatusCode());
    }

    @Test
    void webClientConfig_entregaUnBuilder() {
        assertNotNull(new WebClientConfig().webClientBuilder());
    }
}
