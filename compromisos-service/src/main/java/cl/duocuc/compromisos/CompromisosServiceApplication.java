package cl.duocuc.compromisos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "PagoFlex - Compromisos", version = "1.0", description = "Empresas y compromisos de pago con sus cuotas, estados, anulación y resúmenes"))
public class CompromisosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompromisosServiceApplication.class, args);
    }
}
