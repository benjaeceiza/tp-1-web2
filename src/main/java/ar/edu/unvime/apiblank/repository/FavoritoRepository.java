package ar.edu.unvime.apiblank.repository;

import java.util.List;
import java.util.Optional;

import ar.edu.unvime.apiblank.domain.Favorito;

public interface FavoritoRepository {
    Favorito save(Favorito favorito);
    List<Favorito> findAll();
    Optional<Favorito> findById(Long id);
    void deleteById(Long id);
    List<Favorito> findByListaId(Long listaId);
}