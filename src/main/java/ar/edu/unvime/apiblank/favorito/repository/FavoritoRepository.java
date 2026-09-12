package ar.edu.unvime.apiblank.favorito.repository;

import java.util.List;
import java.util.Optional;

import ar.edu.unvime.apiblank.favorito.model.Favorito;

public interface FavoritoRepository {
    Favorito save(Favorito favorito);
    List<Favorito> findAll();
    Optional<Favorito> findById(Long id);
    void deleteById(Long id);
}