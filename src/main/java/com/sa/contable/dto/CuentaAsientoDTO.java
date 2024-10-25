package com.sa.contable.dto;

import java.math.BigDecimal;

public class CuentaAsientoDTO {
    private String cuentaNombre;
    private Long cuentaCodigo; // Este atributo se puede dejar como Long si el código de cuenta sigue siendo un número entero
    private BigDecimal debe; // Cambiado a BigDecimal
    private BigDecimal haber; // Cambiado a BigDecimal
    private Long asientoId;
    private BigDecimal saldo; // Cambiado a BigDecimal
    private Long id;

    public CuentaAsientoDTO(Long cuentaCodigo, BigDecimal debe, BigDecimal haber, Long asientoId, BigDecimal saldo) {
        this.cuentaCodigo = cuentaCodigo;
        this.debe = debe;
        this.haber = haber;
        this.asientoId = asientoId;
        this.saldo = saldo;
    }

    public CuentaAsientoDTO() {
    }

    // Getters y setters

    public String getCuentaNombre() {
        return cuentaNombre;
    }

    public void setCuentaNombre(String cuentaNombre) {
        this.cuentaNombre = cuentaNombre;
    }
    
    public Long getCuentaCodigo() {
        return cuentaCodigo;
    }

    public void setCuentaCodigo(Long cuentaCodigo) {
        this.cuentaCodigo = cuentaCodigo;
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

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
