package com.sa.contable.DTO;

import com.sa.contable.Cuenta.Cuenta;

public class CuentaMovimientoDTO {
    private Cuenta cuenta;
    private Double monto;
    private String tipo; // "DEBE" o "HABER"

    public CuentaMovimientoDTO(Cuenta cuenta, double monto, String tipo) {

        this.cuenta = cuenta;

        this.monto = monto;

        this.tipo = tipo;

    }

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