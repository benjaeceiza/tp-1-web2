package ar.edu.unvime.apiblank.producto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unvime.apiblank.producto.dto.ProductoDTO;
import ar.edu.unvime.apiblank.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Consumo de la API externa DummyJSON")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene una lista paginada de productos desde DummyJSON filtrada por nuestro DTO")
    public List<ProductoDTO> obtenerTodos(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "30") int limit) {
        return productoService.obtenerTodos(skip, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene un producto específico desde DummyJSON")
    public ProductoDTO obtenerProductoPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }
}