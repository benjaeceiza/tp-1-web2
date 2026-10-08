package ar.edu.unvime.apiblank.dto.lista;

import jakarta.validation.constraints.NotBlank;

public record ListaRequestDTO(
        @NotBlank(message = "El nombre de la lista no puede estar vacío")
        String nombre
) {
}