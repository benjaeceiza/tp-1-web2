package ar.edu.unvime.apiblank.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
    List<ListaEntity> findByUsuarioId(Long usuarioId);
}
