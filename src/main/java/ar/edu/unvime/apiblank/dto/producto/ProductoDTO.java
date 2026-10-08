package ar.edu.unvime.apiblank.dto.producto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoDTO(
    Long id,
    @JsonProperty("title") String nombre,
    @JsonProperty("price") BigDecimal precio,
    int stock
) {}