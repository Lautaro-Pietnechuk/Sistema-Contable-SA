package com.sa.contable.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LibroMayorDTO {
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal debe;
    private BigDecimal haber;
    private BigDecimal saldo;
    private String tipoCuenta;  // Nueva propiedad para tipoCuenta

    // Constructor actualizado
    public LibroMayorDTO(LocalDate fecha, String descripcion, BigDecimal debe, BigDecimal haber, BigDecimal saldo, String tipoCuenta) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.debe = debe;
        this.haber = haber;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;  // Inicializa el tipoCuenta
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

    public String getTipoCuenta() {
        return tipoCuenta;  // Método para obtener tipoCuenta
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;  // Método para establecer tipoCuenta
    }
}
