package com.sa.contable.dto;

public class CuentaAsientoDTO {
    private String cuentaNombre;
    private Long cuentaCodigo;
    private Double debe;
    private Double haber;
    private Long asientoId;
    private Double saldo;
    private Long id;

    public CuentaAsientoDTO(long cuentaCodigo, double debe, double haber, long asientoId, double saldo) {
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

    public Double getDebe() {
        return debe;
    }

    public void setDebe(Double debe) {
        this.debe = debe;
    }

    public Double getHaber() {
        return haber;
    }

    public void setHaber(Double haber) {
        this.haber = haber;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}


