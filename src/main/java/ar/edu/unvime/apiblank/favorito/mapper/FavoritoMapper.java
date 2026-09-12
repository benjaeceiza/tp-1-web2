package ar.edu.unvime.apiblank.favorito.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import ar.edu.unvime.apiblank.favorito.dto.FavoritoRequestDTO;
import ar.edu.unvime.apiblank.favorito.dto.FavoritoResponseDTO;
import ar.edu.unvime.apiblank.favorito.model.Favorito;

@Component
public class FavoritoMapper {

    public Favorito toEntity(FavoritoRequestDTO dto) {
        return new Favorito(
                null,
                dto.productoId(),
                dto.notaPersonal(),
                LocalDateTime.now()
        );
    }

    public FavoritoResponseDTO toDTO(Favorito entidad) {
        return new FavoritoResponseDTO(
                entidad.getId(),
                entidad.getProductoId(),
                entidad.getNotaPersonal(),
                entidad.getFechaAgregado()
        );
    }
}