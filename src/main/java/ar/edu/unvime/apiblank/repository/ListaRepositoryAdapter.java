package ar.edu.unvime.apiblank.repository;

import org.springframework.stereotype.Repository;
import ar.edu.unvime.apiblank.domain.Lista;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity entity = new ListaEntity();
        entity.setId(lista.getId());
        entity.setNombre(lista.getNombre());

        if (lista.getUsuarioId() != null) {
            UsuarioEntity usuarioRef = new UsuarioEntity();
            usuarioRef.setId(lista.getUsuarioId());
            entity.setUsuario(usuarioRef);
        }

        ListaEntity guardada = jpaRepository.save(entity);
        lista.setId(guardada.getId());
        return lista;
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Lista toDomain(ListaEntity e) {
        Long usuarioId = e.getUsuario() != null ? e.getUsuario().getId() : null;
        return new Lista(e.getId(), e.getNombre(), usuarioId);
    }
}
