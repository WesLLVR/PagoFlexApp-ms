package cl.duocuc.compromisos;

import cl.duocuc.compromisos.client.UsuarioClient;
import cl.duocuc.compromisos.config.WebClientConfig;
import cl.duocuc.compromisos.dto.CrearPersonaDto;
import cl.duocuc.compromisos.dto.UsuarioDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioClientTest {

    private static final String CAMILA = "{\"id\":1,\"nombre\":\"Camila Rojas\",\"rut\":\"15.834.207-3\",\"correo\":\"camila@pagoflex.cl\"}";

    private UsuarioClient cliente(HttpStatus estado, String cuerpo) {
        ExchangeFunction respuesta = solicitud -> Mono.just(ClientResponse.create(estado)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(cuerpo)
                .build());
        return new UsuarioClient(WebClient.builder().exchangeFunction(respuesta), "http://usuarios-service");
    }

    private UsuarioClient clienteSinConexion() {
        ExchangeFunction caido = solicitud -> Mono.error(new WebClientRequestException(
                new IOException("Conexión rechazada"), HttpMethod.GET, URI.create("http://usuarios-service"), HttpHeaders.EMPTY));
        return new UsuarioClient(WebClient.builder().exchangeFunction(caido), "http://usuarios-service");
    }

    private final CrearPersonaDto ana = new CrearPersonaDto("Ana Torres", "20.111.222-3", "ana@pagoflex.cl", "912345678");

    @Test
    void buscarPorRut_devuelveLaPersonaONullSiNoExiste() {
        UsuarioDto persona = cliente(HttpStatus.OK, CAMILA).buscarPorRut("15.834.207-3");

        assertEquals("Camila Rojas", persona.nombre());
        assertNull(cliente(HttpStatus.NOT_FOUND, "{\"message\":\"No hay\"}").buscarPorRut("11.111.111-1"));
    }

    @Test
    void crear_devuelveLaPersonaCreada() {
        assertEquals(1L, cliente(HttpStatus.CREATED, CAMILA).crear(ana).id());
    }

    @Test
    void crear_conError_propagaElMensajeDeUsuarios() {
        ResponseStatusException conflicto = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.CONFLICT, "{\"message\":\"Ese correo ya está registrado\"}").crear(ana));
        ResponseStatusException sinMensaje = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.BAD_REQUEST, "{}").crear(ana));
        ResponseStatusException cuerpoInvalido = assertThrows(ResponseStatusException.class,
                () -> cliente(HttpStatus.BAD_REQUEST, "no es json").crear(ana));

        assertEquals(HttpStatus.CONFLICT, conflicto.getStatusCode());
        assertEquals("Ese correo ya está registrado", conflicto.getReason());
        assertEquals("No se pudo registrar a la persona", sinMensaje.getReason());
        assertEquals("No se pudo registrar a la persona", cuerpoInvalido.getReason());
    }

    @Test
    void sinConexion_responde503() {
        UsuarioClient caido = clienteSinConexion();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> caido.buscarPorRut("15.834.207-3")).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, () -> caido.crear(ana)).getStatusCode());
    }

    @Test
    void webClientConfig_entregaUnBuilder() {
        assertNotNull(new WebClientConfig().webClientBuilder());
    }
}
