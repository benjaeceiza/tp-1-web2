package ar.edu.unvime.apiblank.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ar.edu.unvime.apiblank.dto.lista.ListaRequestDTO;
import ar.edu.unvime.apiblank.dto.lista.ListaResponseDTO;
import ar.edu.unvime.apiblank.dto.lista.MoverFavoritosRequestDTO;
import ar.edu.unvime.apiblank.dto.favorito.FavoritoResponseDTO;
import ar.edu.unvime.apiblank.service.ListaService;
import ar.edu.unvime.apiblank.service.FavoritoService;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Operaciones para gestionar las listas de favoritos")
public class ListaController {

    private final ListaService listaService;
    private final FavoritoService favoritoService;

    public ListaController(ListaService listaService, FavoritoService favoritoService) {
        this.listaService = listaService;
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva lista", description = "Crea una lista vacía para organizar los favoritos.")
    public ListaResponseDTO crear(@Valid @RequestBody ListaRequestDTO request) {
        return listaService.guardar(request);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las listas", description = "Devuelve un arreglo con todas las listas creadas.")
    public List<ListaResponseDTO> listar() {
        return listaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista por ID", description = "Busca una lista específica a partir de su ID.")
    public ListaResponseDTO obtenerUna(@PathVariable Long id) {
        return listaService.obtenerPorId(id);
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Ver favoritos de una lista", description = "Devuelve todos los favoritos que pertenecen a una lista específica.")
    public List<FavoritoResponseDTO> favoritosDeUnaLista(@PathVariable Long id) {
        listaService.obtenerPorId(id); 
        return favoritoService.obtenerPorListaId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una lista", description = "Elimina una lista. Retorna error 409 si la lista aún contiene favoritos.")
    public void eliminar(@PathVariable Long id) {
        listaService.eliminar(id);
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mover favoritos entre listas", description = "Transfiere todos los favoritos de la lista origen a la destino y luego elimina la lista origen.")
    public void moverFavoritos(
            @PathVariable Long origenId, 
            @Valid @RequestBody MoverFavoritosRequestDTO request) {
        listaService.moverFavoritos(origenId, request.destinoId());
    }
}