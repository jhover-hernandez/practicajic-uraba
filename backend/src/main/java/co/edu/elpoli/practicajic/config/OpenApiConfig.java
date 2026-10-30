package co.edu.elpoli.practicajic.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** RNF-12: documentación en /swagger-ui.html. El botón "Authorize" recibe el accessToken del login. */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "PRACTICAJIC URABÁ API", version = "0.1.0",
                description = "API REST de la plataforma de prácticas del Politécnico JIC – Regional Urabá"),
        security = @SecurityRequirement(name = "bearer"))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
