package ar.edu.unvime.apiblank.favorito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoritoRequestDTO(
    @NotNull(message = "El ID del producto es obligatorio")
    Long productoId,

    @NotBlank(message = "La nota personal no puede estar vacía")
    String notaPersonal
) {}