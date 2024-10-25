package com.sa.contable.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class CuentaAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cuenta_codigo", nullable = false)
    private Cuenta cuenta; // Relación con la entidad Cuenta

    @ManyToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento; // Relación con la entidad Asiento

    @Column(nullable = false)
    private BigDecimal debe; // Cambiado de Double a BigDecimal

    @Column(nullable = false)
    private BigDecimal haber; // Cambiado de Double a BigDecimal

    @Column(nullable = false)
    private BigDecimal saldo; // Cambiado de Double a BigDecimal

    public CuentaAsiento() {
    }

    @Override
    public String toString() {
        return "CuentaAsiento{" +
                "cuenta=" + (cuenta != null ? cuenta.getNombre() : "null") + // Cambia esto según tus atributos
                ", debe=" + debe +
                ", haber=" + haber +
                ", saldo=" + saldo +
                '}';
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getCuenta() {
        return cuenta; // Devuelve el objeto Cuenta
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta; // Recibe un objeto Cuenta directamente
    }

    public Asiento getAsiento() {
        return asiento; // Devuelve el objeto Asiento
    }

    public void setAsiento(Asiento asiento) {
        this.asiento = asiento; // Recibe un objeto Asiento directamente
    }

    public BigDecimal getDebe() {
        return debe; // Cambiado a BigDecimal
    }

    public void setDebe(BigDecimal debe) {
        this.debe = debe; // Cambiado a BigDecimal
    }

    public BigDecimal getHaber() {
        return haber; // Cambiado a BigDecimal
    }

    public void setHaber(BigDecimal haber) {
        this.haber = haber; // Cambiado a BigDecimal
    }

    public BigDecimal getSaldo() {
        return saldo; // Cambiado a BigDecimal
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo; // Cambiado a BigDecimal
    }
}
