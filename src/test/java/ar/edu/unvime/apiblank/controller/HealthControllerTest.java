package ar.edu.unvime.apiblank.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void health_devuelveStatusOkYService() {
        Map<String, String> respuesta = new HealthController().health();

        assertThat(respuesta)
                .containsEntry("status", "ok")
                .containsEntry("service", "api-blank");
    }
}
