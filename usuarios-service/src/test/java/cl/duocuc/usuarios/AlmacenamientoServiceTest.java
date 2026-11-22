package cl.duocuc.usuarios;

import cl.duocuc.usuarios.service.AlmacenamientoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlmacenamientoServiceTest {

    @TempDir
    Path carpeta;

    @Test
    void guardar_creaElArchivoYSePuedeLeerYEliminar() throws Exception {
        AlmacenamientoService almacenamiento = new AlmacenamientoService(carpeta.toString());

        String nombre = almacenamiento.guardar(new MockMultipartFile("archivo", "foto.png", "image/png", new byte[]{4, 5}));

        assertTrue(nombre.endsWith(".jpg"));
        assertArrayEquals(new byte[]{4, 5}, almacenamiento.leer(nombre));
        almacenamiento.eliminar(nombre);
        assertFalse(Files.exists(carpeta.resolve(nombre)));
    }

    @Test
    void guardar_rechazaArchivosVaciosOQueNoSonImagen() {
        AlmacenamientoService almacenamiento = new AlmacenamientoService(carpeta.toString());

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> almacenamiento.guardar(
                new MockMultipartFile("archivo", "vacio.jpg", "image/jpeg", new byte[0]))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () -> almacenamiento.guardar(
                new MockMultipartFile("archivo", "datos.txt", "text/plain", new byte[]{1}))).getStatusCode());
    }

    @Test
    void leer_archivoInexistente_responde404YEliminarNullNoFalla() {
        AlmacenamientoService almacenamiento = new AlmacenamientoService(carpeta.resolve("nueva").toString());

        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> almacenamiento.leer("no-existe.jpg")).getStatusCode());
        assertDoesNotThrow(() -> almacenamiento.eliminar(null));
    }
}
