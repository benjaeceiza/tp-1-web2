package ar.edu.unvime.apiblank.producto.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoDTO(
    Long id,
    @JsonProperty("title") String nombre,
    @JsonProperty("price") BigDecimal precio,
    int stock
) {}