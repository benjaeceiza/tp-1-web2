package ar.edu.unvime.apiblank.dto.favorito;

import java.time.LocalDateTime;

public record FavoritoResponseDTO(
        Long id,
        Long listaId,
        Long productoId,
        String notaPersonal,
        LocalDateTime fechaAgregado
) {
}