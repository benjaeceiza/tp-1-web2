package ar.edu.unvime.apiblank.repository;
import java.util.List;
import java.util.Optional;
import ar.edu.unvime.apiblank.domain.Lista;

public interface ListaRepository {
    Lista save(Lista lista);
    List<Lista> findAll();
    Optional<Lista> findById(Long id);
    void deleteById(Long id);
}