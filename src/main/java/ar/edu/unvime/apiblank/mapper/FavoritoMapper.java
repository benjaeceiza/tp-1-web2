package ar.edu.unvime.apiblank.mapper;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import ar.edu.unvime.apiblank.domain.Favorito;
import ar.edu.unvime.apiblank.dto.favorito.FavoritoRequestDTO;
import ar.edu.unvime.apiblank.dto.favorito.FavoritoResponseDTO;

@Component
public class FavoritoMapper {

    public Favorito toDomain(FavoritoRequestDTO dto) {
        return new Favorito(
                null,
                dto.listaId(),       
                dto.productoId(),
                dto.notaPersonal(),
                LocalDateTime.now()
        );
    }

    public FavoritoResponseDTO toDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getListaId(), 
                favorito.getProductoId(),
                favorito.getNotaPersonal(),
                favorito.getFechaAgregado()
        );
    }
}