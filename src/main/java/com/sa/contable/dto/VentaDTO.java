// filepath: d:\Documentos\GitHub\Sistema-Contable-SA\src\main\java\com\sa\contable\dto\VentaDTO.java
package com.sa.contable.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VentaDTO {

    private Long id;
    private String numeroComprobante;
    private LocalDateTime fecha;
    private Long clienteId;
    private String clienteNombre;
    private List<DetalleVentaDTO> detalles;
    private Double iva;
    private Double total;
    private String observaciones;
    private Boolean anulada;

    public VentaDTO() {
    }

    public VentaDTO(Long id, String numeroComprobante, LocalDateTime fecha, Long clienteId,
                    String clienteNombre, List<DetalleVentaDTO> detalles,
                    Double iva, Double total, String observaciones, Boolean anulada) {
        this.id = id;
        this.numeroComprobante = numeroComprobante;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.detalles = detalles;
        this.iva = iva;
        this.total = total;
        this.observaciones = observaciones;
        this.anulada = anulada;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public List<DetalleVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaDTO> detalles) {
        this.detalles = detalles;
    }


    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }
}