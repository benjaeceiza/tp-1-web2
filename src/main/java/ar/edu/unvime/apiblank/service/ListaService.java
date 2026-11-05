package ar.edu.unvime.apiblank.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import ar.edu.unvime.apiblank.domain.Lista;
import ar.edu.unvime.apiblank.domain.Favorito;
import ar.edu.unvime.apiblank.dto.lista.ListaRequestDTO;
import ar.edu.unvime.apiblank.dto.lista.ListaResponseDTO;
import ar.edu.unvime.apiblank.exception.AccesoDenegadoException;
import ar.edu.unvime.apiblank.exception.RecursoNoEncontradoException;
import ar.edu.unvime.apiblank.mapper.ListaMapper;
import ar.edu.unvime.apiblank.repository.ListaRepository;
import ar.edu.unvime.apiblank.repository.FavoritoRepository;
import ar.edu.unvime.apiblank.security.ContextoSeguridad;

@Service
public class ListaService {
    
    private final ListaRepository repository;
    private final ListaMapper mapper;
    private final FavoritoRepository favoritoRepository;
    private final ContextoSeguridad contexto;

    public ListaService(
            ListaRepository repository,
            ListaMapper mapper,
            FavoritoRepository favoritoRepository,
            ContextoSeguridad contexto) {
        this.repository = repository;
        this.mapper = mapper;
        this.favoritoRepository = favoritoRepository;
        this.contexto = contexto;
    }

    public ListaResponseDTO guardar(ListaRequestDTO request) {
        Lista lista = mapper.toDomain(request);
        // Al crear una lista, el dueño es el usuario autenticado — nunca un dato del body
        lista.setUsuarioId(contexto.usuarioActualId());
        return mapper.toDTO(repository.save(lista));
    }

    public List<ListaResponseDTO> obtenerTodas() {
        List<Lista> listas = contexto.esAdmin()
                ? repository.findAll()
                : repository.findByUsuarioId(contexto.usuarioActualId());

        return listas.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ListaResponseDTO obtenerPorId(Long id) {
        Lista lista = buscarPropiaOFallar(id);
        return mapper.toDTO(lista);
    }

    public void eliminar(Long id) {
        buscarPropiaOFallar(id);
        repository.deleteById(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        // 1. Valida que ambas listas existan (404) y pertenezcan al usuario autenticado o sea ADMIN (403)
        buscarPropiaOFallar(origenId);
        buscarPropiaOFallar(destinoId);

        // 2. Reasigna todos los favoritos de la lista origen a la destino
        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito fav : favoritos) {
            fav.setListaId(destinoId);
            favoritoRepository.save(fav); // El adapter se encarga de hacer el UPDATE en BD
        }

        // 3. Elimina la lista origen
        repository.deleteById(origenId);
    }

    private Lista buscarPropiaOFallar(Long id) {
        Lista lista = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la lista con id: " + id));

        boolean esDueno = lista.getUsuarioId() != null
                && lista.getUsuarioId().equals(contexto.usuarioActualId());

        if (!esDueno && !contexto.esAdmin()) {
            throw new AccesoDenegadoException("La lista " + id + " no te pertenece");
        }
        return lista;
    }
}
