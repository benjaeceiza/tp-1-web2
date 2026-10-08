package ar.edu.unvime.apiblank.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ar.edu.unvime.apiblank.domain.Favorito;

@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Favorito save(Favorito favorito) {
        FavoritoEntity entity = new FavoritoEntity();
        entity.setId(favorito.getId());
        entity.setProductoId(favorito.getProductoId());
        entity.setNota(favorito.getNotaPersonal());
        entity.setFechaAlta(favorito.getFechaAgregado());

        // ACA CONECTAMOS EL FAVORITO CON SU LISTA
        ListaEntity listaRef = new ListaEntity();
        listaRef.setId(favorito.getListaId());
        entity.setLista(listaRef);

        FavoritoEntity savedEntity = jpaRepository.save(entity);

        favorito.setId(savedEntity.getId());
        return favorito;
    }

    @Override
    public List<Favorito> findAll() {
        return jpaRepository.findAll().stream()
                .map(entity -> new Favorito(
                        entity.getId(),
                        entity.getLista().getId(), // Sacamos el ID de la lista
                        entity.getProductoId(),
                        entity.getNota(),
                        entity.getFechaAlta()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> new Favorito(
                        entity.getId(),
                        entity.getLista().getId(),
                        entity.getProductoId(),
                        entity.getNota(),
                        entity.getFechaAlta()
                ));
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId).stream()
                .map(entity -> new Favorito(
                        entity.getId(),
                        entity.getLista().getId(),
                        entity.getProductoId(),
                        entity.getNota(),
                        entity.getFechaAlta()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}