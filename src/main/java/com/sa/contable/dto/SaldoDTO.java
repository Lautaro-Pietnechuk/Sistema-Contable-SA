package com.sa.contable.dto;

public class SaldoDTO {
    private Long codigoCuenta; // Código de la cuenta
    private Long saldo; // Saldo actual

    // Constructor
    public SaldoDTO(Long codigoCuenta, Long saldo) {
        this.codigoCuenta = codigoCuenta;
        this.saldo = saldo;
    }

    // Getters y Setters
    public Long getCodigoCuenta() {
        return codigoCuenta;
    }

    public void setCodigoCuenta(Long codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
    }
}
