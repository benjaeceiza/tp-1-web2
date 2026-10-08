package ar.edu.unvime.apiblank.domain;

import java.time.LocalDateTime;

public class Favorito {
    private Long id;
    private Long listaId; 
    private Long productoId;
    private String notaPersonal;
    private LocalDateTime fechaAgregado;

    
    public Favorito(Long id, Long listaId, Long productoId, String notaPersonal, LocalDateTime fechaAgregado) {
        this.id = id;
        this.listaId = listaId;
        this.productoId = productoId;
        this.notaPersonal = notaPersonal;
        this.fechaAgregado = fechaAgregado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getListaId() { return listaId; } // <-- NUEVO GETTER
    public void setListaId(Long listaId) { this.listaId = listaId; } // <-- NUEVO SETTER
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNotaPersonal() { return notaPersonal; }
    public void setNotaPersonal(String notaPersonal) { this.notaPersonal = notaPersonal; }
    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}