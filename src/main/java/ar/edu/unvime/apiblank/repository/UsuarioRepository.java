package ar.edu.unvime.apiblank.repository;

import java.util.List;
import java.util.Optional;
import ar.edu.unvime.apiblank.domain.Usuario;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(Long id);
}
