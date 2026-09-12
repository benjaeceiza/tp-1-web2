package ar.edu.unvime.apiblank.favorito.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import ar.edu.unvime.apiblank.favorito.model.Favorito;

@Repository
public class FavoritoRepositoryImpl implements FavoritoRepository {
    
    private final List<Favorito> favoritos = new ArrayList<>();
    private final AtomicLong generadorId = new AtomicLong(1);

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setId(generadorId.getAndIncrement());
            favoritos.add(favorito);
        } else {
            favoritos.removeIf(f -> f.getId().equals(favorito.getId()));
            favoritos.add(favorito);
        }
        return favorito;
    }

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(favoritos);
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return favoritos.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        favoritos.removeIf(f -> f.getId().equals(id));
    }
}