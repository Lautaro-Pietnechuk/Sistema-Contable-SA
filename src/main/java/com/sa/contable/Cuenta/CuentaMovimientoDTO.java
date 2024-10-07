package com.sa.contable.Cuenta;

public class CuentaMovimientoDTO {
    private Cuenta cuenta;
    private Double monto;
    private String tipo; // "DEBE" o "HABER"

    // Getters y Setters
    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}