package com.sa.contable.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LibroMayorDTO {
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal debe;  // Nueva propiedad para el debe
    private BigDecimal haber;  // Nueva propiedad para el haber
    private BigDecimal saldo;

    // Constructor actualizado
    public LibroMayorDTO(LocalDate fecha, String descripcion, BigDecimal debe, BigDecimal haber, BigDecimal saldo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.debe = debe;        // Inicializa el debe
        this.haber = haber;      // Inicializa el haber
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

    public BigDecimal getDebe() {
        return debe;
    }

    public void setDebe(BigDecimal debe) {
        this.debe = debe;
    }

    public BigDecimal getHaber() {
        return haber;
    }

    public void setHaber(BigDecimal haber) {
        this.haber = haber;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
