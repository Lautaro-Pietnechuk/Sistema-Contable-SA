package com.sa.contable.DTO;

import java.time.LocalDateTime;

public class CobroResponse {
    private Long id;
    private LocalDateTime fecha;
    private Double monto;
    private String metodoPago;
    private Boolean anulado;

    public CobroResponse(Long id, LocalDateTime fecha, Double monto, String metodoPago, Boolean anulado) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.anulado = anulado;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public Boolean getAnulado() {
        return anulado;
    }
}
