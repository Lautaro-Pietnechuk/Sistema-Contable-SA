package com.sa.contable.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LibroMayorDTO {
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal saldo;

    public LibroMayorDTO(LocalDate fecha, String descripcion, BigDecimal saldo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.saldo = saldo;
    }

    // Getters y Setters
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
