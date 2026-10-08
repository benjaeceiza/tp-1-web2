package ar.edu.unvime.apiblank.dto.producto;

import java.util.List;

public record ProductoResponseDTO(
    List<ProductoDTO> products
) {}