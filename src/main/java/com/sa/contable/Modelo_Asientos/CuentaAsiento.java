package com.sa.contable.Modelo_Asientos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CuentaAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta idCuenta;

    @ManyToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento idAsiento;

    @Column(nullable = false)
    private Double debe;

    @Column(nullable = false)
    private Double haber;

    @Column(nullable = false)
    private Double saldo;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuenta getIdCuenta() {
        return idCuenta;
    }

    public void setCuenta(Cuenta idCuenta) {
        this.idCuenta = idCuenta;
    }

    public Asiento getidAsiento() {
        return idAsiento;
    }

    public void setidAsiento(Asiento idAsiento) {
        this.idAsiento = idAsiento;
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

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }
}