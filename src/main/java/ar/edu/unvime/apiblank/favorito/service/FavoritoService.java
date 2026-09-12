package ar.edu.unvime.apiblank.favorito.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ar.edu.unvime.apiblank.exception.RecursoNoEncontradoException;
import ar.edu.unvime.apiblank.favorito.dto.FavoritoRequestDTO;
import ar.edu.unvime.apiblank.favorito.dto.FavoritoResponseDTO;
import ar.edu.unvime.apiblank.favorito.mapper.FavoritoMapper;
import ar.edu.unvime.apiblank.favorito.model.Favorito;
import ar.edu.unvime.apiblank.favorito.repository.FavoritoRepository;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final FavoritoMapper favoritoMapper;

    public FavoritoService(FavoritoRepository favoritoRepository, FavoritoMapper favoritoMapper) {
        this.favoritoRepository = favoritoRepository;
        this.favoritoMapper = favoritoMapper;
    }

    public FavoritoResponseDTO guardarFavorito(FavoritoRequestDTO requestDTO) {
        Favorito nuevaEntidad = favoritoMapper.toEntity(requestDTO);
        Favorito entidadGuardada = favoritoRepository.save(nuevaEntidad);
        return favoritoMapper.toDTO(entidadGuardada);
    }

    public List<FavoritoResponseDTO> obtenerTodos() {
        return favoritoRepository.findAll().stream()
                .map(favoritoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        return favoritoRepository.findById(id)
                .map(favoritoMapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el favorito con id: " + id));
    }

    public FavoritoResponseDTO actualizarFavorito(Long id, FavoritoRequestDTO requestDTO) {
        Favorito existente = favoritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el favorito con id: " + id));

        existente.setProductoId(requestDTO.productoId());
        existente.setNotaPersonal(requestDTO.notaPersonal());

        Favorito guardado = favoritoRepository.save(existente);
        return favoritoMapper.toDTO(guardado);
    }

    public void eliminarFavorito(Long id) {
        favoritoRepository.deleteById(id);
    }
}