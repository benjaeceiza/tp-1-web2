package ar.edu.unvime.apiblank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import ar.edu.unvime.apiblank.dto.producto.ProductoDTO;
import ar.edu.unvime.apiblank.dto.producto.ProductoResponseDTO;

@Service
public class ProductoService {

    private final RestClient restClient;

    public ProductoService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://dummyjson.com/products")
                .build();
    }

    public List<ProductoDTO> obtenerTodos(int skip, int limit) {
        ProductoResponseDTO response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("skip", skip)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(ProductoResponseDTO.class);

        return response != null ? response.products() : List.of();
    }

    public ProductoDTO obtenerPorId(Long id) {
        return restClient.get()
                .uri("/{id}", id)
                .retrieve()
                .body(ProductoDTO.class);
    }
}
