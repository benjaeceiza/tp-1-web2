package ar.edu.unvime.apiblank.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Listas", description = "Operaciones para gestionar las listas de favoritos. Requiere autenticación (JWT Bearer): cada usuario opera únicamente sobre sus propias listas, salvo rol ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class ListaController {

    private final ListaService listaService;
    private final FavoritoService favoritoService;

    public ListaController(ListaService listaService, FavoritoService favoritoService) {
        this.listaService = listaService;
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear una nueva lista (Requiere autenticación)",
            description = "Crea una lista vacía asociada automáticamente al usuario autenticado en el token JWT. Retorna 401 si no se envía un token válido."
    )
    public ListaResponseDTO crear(@Valid @RequestBody ListaRequestDTO request) {
        return listaService.guardar(request);
    }

    @GetMapping
    @Operation(
            summary = "Obtener listas (Requiere autenticación)",
            description = "Devuelve únicamente las listas pertenecientes al usuario autenticado (o todas las listas del sistema si el usuario tiene rol ADMIN). Retorna 401 sin token."
    )
    public List<ListaResponseDTO> listar() {
        return listaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una lista por ID (Requiere autenticación)",
            description = "Busca una lista por su ID. Requiere ser el dueño de la lista o tener rol ADMIN (retorna 403 Forbidden si pertenece a otro usuario, 404 si no existe, 401 sin token)."
    )
    public ListaResponseDTO obtenerUna(@PathVariable Long id) {
        return listaService.obtenerPorId(id);
    }

    @GetMapping("/{id}/favoritos")
    @Operation(
            summary = "Ver favoritos de una lista (Requiere autenticación)",
            description = "Devuelve todos los favoritos de una lista específica. Valida que la lista pertenezca al usuario autenticado o sea ADMIN (403 si es ajena)."
    )
    public List<FavoritoResponseDTO> favoritosDeUnaLista(@PathVariable Long id) {
        listaService.obtenerPorId(id); 
        return favoritoService.obtenerPorListaId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Eliminar una lista (Requiere autenticación)",
            description = "Elimina una lista propia (o cualquiera si es ADMIN). Retorna 403 si la lista es ajena, 409 si aún contiene favoritos y 401 sin token."
    )
    public void eliminar(@PathVariable Long id) {
        listaService.eliminar(id);
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Mover favoritos entre listas (Requiere autenticación)",
            description = "Transfiere todos los favoritos de la lista origen a la destino y elimina la lista origen. Ambas listas deben pertenecer al usuario autenticado (o ser ADMIN); retorna 403 en caso contrario."
    )
    public void moverFavoritos(
            @PathVariable Long origenId, 
            @Valid @RequestBody MoverFavoritosRequestDTO request) {
        listaService.moverFavoritos(origenId, request.destinoId());
    }
}
