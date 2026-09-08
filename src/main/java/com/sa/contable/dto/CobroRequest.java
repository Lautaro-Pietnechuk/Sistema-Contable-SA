package com.sa.contable.DTO;


public class CobroRequest {
    private Long clienteId;
    private Double monto;
    private String metodoPago; // "EFECTIVO", "TRANSFERENCIA", etc.

    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}