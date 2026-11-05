package ar.edu.unvime.apiblank.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ar.edu.unvime.apiblank.domain.Usuario;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setEmail(usuario.getEmail());
        entity.setPasswordHash(usuario.getPasswordHash());
        entity.setRol(usuario.getRol());

        UsuarioEntity guardado = jpaRepository.save(entity);
        usuario.setId(guardado.getId());
        return usuario;
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream()
                .map(e -> new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRol()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id)
                .map(e -> new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRol()));
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(e -> new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRol()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
