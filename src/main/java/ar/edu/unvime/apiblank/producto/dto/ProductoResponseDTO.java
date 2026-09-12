package ar.edu.unvime.apiblank.producto.dto;

import java.util.List;

public record ProductoResponseDTO(
    List<ProductoDTO> products
) {}