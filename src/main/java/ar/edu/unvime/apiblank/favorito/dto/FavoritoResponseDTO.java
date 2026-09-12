package ar.edu.unvime.apiblank.favorito.dto;

import java.time.LocalDateTime;

public record FavoritoResponseDTO(
    Long id,
    Long productoId,
    String notaPersonal,
    LocalDateTime fechaAgregado
) {}