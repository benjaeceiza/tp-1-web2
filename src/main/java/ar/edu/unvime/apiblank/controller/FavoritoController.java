package ar.edu.unvime.apiblank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unvime.apiblank.dto.favorito.FavoritoRequestDTO;
import ar.edu.unvime.apiblank.dto.favorito.FavoritoResponseDTO;
import ar.edu.unvime.apiblank.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Gestión de productos favoritos guardados en memoria")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    @Operation(summary = "Listar favoritos", description = "Obtiene la lista completa de favoritos guardados en memoria")
    public List<FavoritoResponseDTO> obtenerTodos() {
        return favoritoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener favorito por ID", description = "Busca un favorito específico por su ID")
    public FavoritoResponseDTO obtenerPorId(@PathVariable Long id) {
        return favoritoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear favorito", description = "Guarda un producto como favorito. Requiere ID del producto y una nota personal.")
    public FavoritoResponseDTO crearFavorito(@Valid @RequestBody FavoritoRequestDTO requestDTO) {
        return favoritoService.guardarFavorito(requestDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar favorito", description = "Modifica el productoId o la nota personal de un favorito existente")
    public FavoritoResponseDTO actualizarFavorito(@PathVariable Long id, @Valid @RequestBody FavoritoRequestDTO requestDTO) {
        return favoritoService.actualizarFavorito(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar favorito", description = "Elimina un favorito de la memoria por su ID")
    public void eliminarFavorito(@PathVariable Long id) {
        favoritoService.eliminarFavorito(id);
    }
}