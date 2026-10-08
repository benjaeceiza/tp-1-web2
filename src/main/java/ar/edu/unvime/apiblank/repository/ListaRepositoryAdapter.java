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
        
        ListaEntity guardada = jpaRepository.save(entity);
        lista.setId(guardada.getId());
        return lista;
    }

    @Override
    public List<Lista> findAll() {
        return jpaRepository.findAll().stream()
                .map(e -> new Lista(e.getId(), e.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return jpaRepository.findById(id)
                .map(e -> new Lista(e.getId(), e.getNombre()));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}