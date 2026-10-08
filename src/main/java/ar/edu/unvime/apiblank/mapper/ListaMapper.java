package ar.edu.unvime.apiblank.mapper;

import org.springframework.stereotype.Component;
import ar.edu.unvime.apiblank.domain.Lista;
import ar.edu.unvime.apiblank.dto.lista.ListaRequestDTO;
import ar.edu.unvime.apiblank.dto.lista.ListaResponseDTO;

@Component
public class ListaMapper {

    public Lista toDomain(ListaRequestDTO dto) {
        return new Lista(
                null,
                dto.nombre()
        );
    }

    public ListaResponseDTO toDTO(Lista lista) {
        return new ListaResponseDTO(
                lista.getId(),
                lista.getNombre()
        );
    }
}