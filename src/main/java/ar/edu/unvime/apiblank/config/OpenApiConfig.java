package ar.edu.unvime.apiblank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API de Favoritos y Listas - TP3 (Web 2 - UNViMe)")
                        .version("1.0.0")
                        .description("API REST con autenticación JWT y autorización por rol/propiedad. "
                                + "Las rutas `/api/auth/**`, `/api/productos/**` y `/health` son públicas. "
                                + "Las rutas `/api/listas/**` y `/api/favoritos/**` requieren autenticación con token JWT (botón Authorize)."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
