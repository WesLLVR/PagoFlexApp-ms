package cl.duocuc.pagos;

import cl.duocuc.pagos.client.CompromisoClient;
import cl.duocuc.pagos.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompromisoClientTest {

    private static final String CUOTA = "{\"id\":10,\"folio\":\"CP-2026-0102\",\"usuarioId\":1,\"numeroCuota\":10,"
            + "\"totalCuotas\":12,\"monto\":15000,\"recargo\":0,\"totalAPagar\":15000,\"diasAtraso\":0,\"vencimiento\":\"2026-12-01\",\"estado\":\"PENDIENTE\"}";

    private final List<ClientRequest> solicitudes = new ArrayList<>();

    private CompromisoClient cliente(HttpStatus estado, String cuerpo) {
        ExchangeFunction respuesta = solicitud -> {
            solicitudes.add(solicitud);
            return Mono.just(ClientResponse.create(estado)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(cuerpo)
                    .build());
        };
        return new CompromisoClient(WebClient.builder().exchangeFunction(respuesta), "http://compromisos-service");
    }

    @Test
    void obtenerYPagable_leenLaRespuesta() {
        assertEquals("CP-2026-0102", cliente(HttpStatus.OK, CUOTA).obtener(10L).folio());
        assertFalse(cliente(HttpStatus.OK, "{\"pagable\":false,\"motivo\":\"Primero paga la cuota 9/12\"}").pagable(10L).pagable());
    }

    @Test
    void cuotasYConsiderados_leenListas() {
        assertEquals(2, cliente(HttpStatus.OK, "[" + CUOTA + "," + CUOTA + "]").cuotas(10L).size());
        assertEquals(1, cliente(HttpStatus.OK, "[" + CUOTA + "]").considerados(1L).size());
    }

    @Test
    void marcarPagado_haceUnPatchAlEstado() {
        cliente(HttpStatus.OK, CUOTA).marcarPagado(10L);

        assertEquals(HttpMethod.PATCH, solicitudes.get(0).method());
        assertEquals("/api/compromisos/10/estado", solicitudes.get(0).url().getPath());
    }

    @Test
    void errores_seConviertenEnResponseStatusException() {
        ResponseStatusException conMensaje = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.CONFLICT, "{\"message\":\"No se puede pasar el compromiso\"}").marcarPagado(10L));
        ResponseStatusException sinMensaje = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.NOT_FOUND, "{}").obtener(99L));
        ResponseStatusException cuerpoInvalido = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.INTERNAL_SERVER_ERROR, "error").obtener(10L));

        assertEquals("No se puede pasar el compromiso", conMensaje.getReason());
        assertEquals(HttpStatus.NOT_FOUND, sinMensaje.getStatusCode());
        assertEquals("Error en el servicio de compromisos", sinMensaje.getReason());
        assertEquals("Error en el servicio de compromisos", cuerpoInvalido.getReason());
    }

    @Test
    void sinConexion_responde503() {
        ExchangeFunction caido = solicitud -> Mono.error(new WebClientRequestException(
                new IOException("Conexión rechazada"), HttpMethod.GET, URI.create("http://compromisos-service"), HttpHeaders.EMPTY));
        CompromisoClient cliente = new CompromisoClient(WebClient.builder().exchangeFunction(caido), "http://compromisos-service");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> cliente.obtener(10L)).getStatusCode());
    }

    @Test
    void webClientConfig_entregaUnBuilder() {
        assertNotNull(new WebClientConfig().webClientBuilder());
    }
}
