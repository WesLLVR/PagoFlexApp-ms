package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Rol;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final String CLAVE_INICIAL = "123456";

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.saveAll(List.of(
                cliente("Camila Andrea Rojas Peña", "15.834.207-3", "camila@pagoflex.cl", "+56 9 0000 0101", "Maipú"),
                cliente("Luis Alberto Muñoz Tapia", "12.476.318-5", "luis@pagoflex.cl", "+56 9 0000 0102", "Puente Alto"),
                cliente("Rosa Elena Contreras Díaz", "6.893.541-5", "rosa@pagoflex.cl", "+56 9 0000 0103", "San Miguel"),
                cliente("Matías Ignacio Soto Vergara", "21.045.873-5", "matias@pagoflex.cl", "+56 9 0000 0104", "La Florida"),
                cliente("Fernanda Paz Godoy Lagos", "17.562.904-1", "fernanda@pagoflex.cl", "+56 9 0000 0105", "Ñuñoa"),
                cliente("Héctor Manuel Pizarro Campos", "10.318.265-4", "hector@pagoflex.cl", "+56 9 0000 0106", "Quilicura"),
                cliente("Javiera Constanza Reyes Molina", "19.287.430-0", "javiera@pagoflex.cl", "+56 9 0000 0107", "San Bernardo"),
                cliente("Benjamín Esteban Araya Fuentes", "18.704.396-4", "benjamin@pagoflex.cl", "+56 9 0000 0108", "Estación Central"),
                agente("Andrés Felipe Lagos Meza", "16.248.317-K", "andres@pagoflex.cl", "Soporte"),
                agente("Daniela Sofía Ortiz Bravo", "18.352.091-1", "daniela@pagoflex.cl", "Soporte"),
                agente("Ricardo José Salinas Cáceres", "13.907.264-2", "ricardo@pagoflex.cl", "Soporte"),
                agente("Valentina Ignacia Paredes Silva", "17.781.435-0", "valentina@pagoflex.cl", "Comercial"),
                agente("Tomás Alejandro Vidal Rojas", "19.564.028-9", "tomas@pagoflex.cl", "Comercial"),
                ejecutivo("Paula Andrea Saavedra Núñez", "14.629.853-2", "paula@pagoflex.cl", "+56 9 0000 0401", 1L, "Ejecutiva de cobranza"),
                ejecutivo("Marco Antonio Fuentes Riquelme", "11.853.720-3", "marco@pagoflex.cl", "+56 9 0000 0402", 2L, "Encargado de finanzas"),
                ejecutivo("Claudia Beatriz Henríquez Soto", "15.370.946-7", "claudia@pagoflex.cl", "+56 9 0000 0403", 3L, "Jefa de cobranza"),
                ejecutivo("Felipe Ignacio Morales Tapia", "17.095.482-3", "felipe@pagoflex.cl", "+56 9 0000 0404", 3L, "Ejecutivo de cobranza"),
                ejecutivo("Gloria Inés Carrasco Vega", "9.846.217-1", "gloria@pagoflex.cl", "+56 9 0000 0405", 6L, "Tesorera"),
                ejecutivo("Sebastián Andrés Núñez Olivares", "18.930.571-0", "sebastian@pagoflex.cl", "+56 9 0000 0406", 7L, "Analista de finanzas"),
                ejecutivo("Mónica Patricia Leiva Castro", "12.584.396-4", "monica@pagoflex.cl", "+56 9 0000 0407", 8L, "Administradora de cobranza")
        ));
    }

    private Usuario cliente(String nombre, String rut, String correo, String telefono, String comuna) {
        return Usuario.builder()
                .nombre(nombre).rut(rut).correo(correo).contrasena(CLAVE_INICIAL)
                .telefono(telefono).comuna(comuna).rol(Rol.USUARIO_FINAL)
                .build();
    }

    private Usuario agente(String nombre, String rut, String correo, String cargo) {
        return Usuario.builder()
                .nombre(nombre).rut(rut).correo(correo).contrasena(CLAVE_INICIAL)
                .rol(Rol.AGENTE).cargo(cargo)
                .build();
    }

    private Usuario ejecutivo(String nombre, String rut, String correo, String telefono, Long empresaId, String cargo) {
        return Usuario.builder()
                .nombre(nombre).rut(rut).correo(correo).contrasena(CLAVE_INICIAL)
                .telefono(telefono).rol(Rol.EJECUTIVO).empresaId(empresaId).cargo(cargo)
                .build();
    }
}
