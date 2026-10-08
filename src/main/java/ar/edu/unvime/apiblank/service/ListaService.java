package ar.edu.unvime.apiblank.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import ar.edu.unvime.apiblank.domain.Lista;
import ar.edu.unvime.apiblank.domain.Favorito;
import ar.edu.unvime.apiblank.dto.lista.ListaRequestDTO;
import ar.edu.unvime.apiblank.dto.lista.ListaResponseDTO;
import ar.edu.unvime.apiblank.exception.RecursoNoEncontradoException;
import ar.edu.unvime.apiblank.mapper.ListaMapper;
import ar.edu.unvime.apiblank.repository.ListaRepository;
import ar.edu.unvime.apiblank.repository.FavoritoRepository; 

@Service
public class ListaService {
    
    private final ListaRepository repository;
    private final ListaMapper mapper;
    private final FavoritoRepository favoritoRepository; 

    // Actualizamos el constructor
    public ListaService(ListaRepository repository, ListaMapper mapper, FavoritoRepository favoritoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.favoritoRepository = favoritoRepository;
    }

    public ListaResponseDTO guardar(ListaRequestDTO request) {
        Lista lista = mapper.toDomain(request);
        return mapper.toDTO(repository.save(lista));
    }

    public List<ListaResponseDTO> obtenerTodas() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la lista con id: " + id));
    }

    public void eliminar(Long id) {
        repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la lista con id: " + id));
        repository.deleteById(id);
    }

    
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        // 1. Valida que ambas listas existan (404 si no)
        repository.findById(origenId).orElseThrow(() -> new RecursoNoEncontradoException("Lista origen no encontrada"));
        repository.findById(destinoId).orElseThrow(() -> new RecursoNoEncontradoException("Lista destino no encontrada"));

        // 2. Reasigna todos los favoritos de la lista origen a la destino
        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav); // El adapter se encarga de hacer el UPDATE en BD
        }

        // 3. Elimina la lista origen
        repository.deleteById(origenId);
    }
}