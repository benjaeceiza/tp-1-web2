package ar.edu.unvime.apiblank.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "mi_clave_secreta_super_segura_para_jwt_tp3_web2_2026";

    @Test
    void generarYValidar_tokenValido_devuelveClaimsCorrectos() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);

        String token = jwtService.generarToken(42L, "USER");
        Claims claims = jwtService.validarYObtenerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("rol", String.class)).isEqualTo("USER");
    }

    @Test
    void validar_tokenFirmadoConOtroSecreto_lanzaExcepcion() {
        JwtService emisor = new JwtService(SECRET, 60_000L);
        JwtService otro = new JwtService("otra_clave_secreta_distinta_pero_de_32_bytes_minimo", 60_000L);

        String token = emisor.generarToken(42L, "USER");

        assertThatThrownBy(() -> otro.validarYObtenerClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validar_tokenVencido_lanzaExcepcion() {
        JwtService jwtService = new JwtService(SECRET, -1_000L);

        String tokenVencido = jwtService.generarToken(42L, "USER");

        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(tokenVencido))
                .isInstanceOf(JwtException.class);
    }
}
