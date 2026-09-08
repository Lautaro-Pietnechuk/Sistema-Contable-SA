package com.sa.contable.DTO;

import java.math.BigDecimal;

public class SaldoDTO {
    private Long codigoCuenta; // Código de la cuenta
    private BigDecimal saldo; // Saldo actual

    // Constructor
    public SaldoDTO(Long codigoCuenta, BigDecimal saldo) {
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

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
