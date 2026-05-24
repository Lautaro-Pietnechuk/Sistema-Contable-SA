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
    private String tipoDePago; // "EFECTIVO", "DEBITO" o "CUENTA_CORRIENTE"
    private String estado = "PENDIENTE"; 

    public VentaDTO() {
    }

    // CORREGIDO: Ahora el constructor recibe y asigna el tipoDePago
    public VentaDTO(Long id, String numeroComprobante, LocalDateTime fecha, Long clienteId,
                    String clienteNombre, List<DetalleVentaDTO> detalles,
                    Double iva, Double total, String observaciones, String tipoDePago, Boolean anulada) {
        this.id = id;
        this.numeroComprobante = numeroComprobante;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.detalles = detalles;
        this.iva = iva;
        this.total = total;
        this.observaciones = observaciones;
        this.tipoDePago = tipoDePago != null ? tipoDePago.toUpperCase() : null;
        this.estado = anulada ? "ANULADA" : "PENDIENTE";
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

    public String getTipoDePago() {
        return tipoDePago;
    }

    // CORREGIDO: Validación blindada contra valores nulos y espacios
    public void setTipoDePago(String tipoDePago) {
        if (tipoDePago == null || tipoDePago.trim().isEmpty()) {
            this.tipoDePago = null;
            return;
        }
        
        String limpio = tipoDePago.trim().toUpperCase();
        if (!limpio.matches("EFECTIVO|DEBITO|CUENTA_CORRIENTE")) {
            throw new IllegalArgumentException("Tipo de pago inválido. Debe ser 'EFECTIVO', 'DEBITO' o 'CUENTA_CORRIENTE'.");
        }
        this.tipoDePago = limpio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (estado.equals("PENDIENTE") || estado.equals("PAGADA") || estado.equals("ANULADA")) {
            this.estado = estado;
        } else {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }
    }
}