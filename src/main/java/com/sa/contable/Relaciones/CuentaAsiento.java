package com.sa.contable.Relaciones;

import com.sa.contable.Asiento.Asiento;
import com.sa.contable.cuenta.Cuenta;

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
    private Cuenta cuenta; // Relación con la entidad Cuenta

    @ManyToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento; // Relación con la entidad Asiento

    @Column(nullable = false)
    private Double debe;

    @Column(nullable = false)
    private Double haber;

    @Column(nullable = false)
    private Double saldo; //saldo parcial con el que queda la cuenta

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCodigoCuenta() {
        return cuenta.getCodigo(); // Devuelve el id de la entidad Cuenta
    }

    public void setCodigoCuenta(Long CodigoCuenta) {
        // Aquí podrías buscar la cuenta por su id o recibir un objeto Cuenta directamente
        this.cuenta = new Cuenta(); // Esto es solo un ejemplo, normalmente tendrías que recuperar la cuenta de la base de datos.
        this.cuenta.setCodigo(CodigoCuenta); // Asignas el id a la entidad
    }

    public Long getIdAsiento() {
        return asiento.getId(); // Devuelve el id de la entidad Asiento
    }

    public void setIdAsiento(Long idAsiento) {
        // Aquí podrías buscar el asiento por su id o recibir un objeto Asiento directamente
        this.asiento = new Asiento(); // Esto es solo un ejemplo, normalmente tendrías que recuperar el asiento de la base de datos.
        this.asiento.setId(idAsiento); // Asignas el id a la entidad
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
