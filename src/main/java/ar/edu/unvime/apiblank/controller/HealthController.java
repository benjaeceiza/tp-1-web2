package ar.edu.unvime.apiblank.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Salud", description = "Chequeo de estado del servicio para monitoreo y despliegue")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del servicio", description = "Endpoint público para el health check de Render.")
    public Map<String, String> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "api-blank");
        return response;
    }
}
