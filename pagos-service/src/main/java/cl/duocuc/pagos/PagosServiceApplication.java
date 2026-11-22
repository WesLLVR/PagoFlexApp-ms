package cl.duocuc.pagos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "PagoFlex - Pagos", version = "1.0", description = "Pago de cuotas, comprobantes, historial y cumplimiento"))
public class PagosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosServiceApplication.class, args);
    }
}
