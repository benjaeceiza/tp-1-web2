package ar.edu.unvime.apiblank.mapper;

import org.springframework.stereotype.Component;
import ar.edu.unvime.apiblank.domain.Usuario;
import ar.edu.unvime.apiblank.dto.usuario.UsuarioResponseDTO;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
