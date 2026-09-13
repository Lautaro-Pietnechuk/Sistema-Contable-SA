package com.sa.contable.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String mail;
    private String telefono;

    @Column(nullable = false, columnDefinition = "double precision default 0")
    private Double saldoPendiente = 0.0;

    // Constructores
    public Cliente() {}

    public Cliente(String nombre, String mail, String telefono) {
        this.nombre = nombre;
        this.mail = mail;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(Double saldoPendiente) {
        if (saldoPendiente == null || saldoPendiente < 0) {
            throw new IllegalArgumentException("El saldo pendiente no puede ser nulo ni negativo.");
        }
        this.saldoPendiente = saldoPendiente;
    }

    public void ajustarSaldoPendiente(Double importe) {
        if (importe == null) {
            throw new IllegalArgumentException("El importe para ajustar el saldo no puede ser nulo.");
        }
        double nuevoSaldo = (saldoPendiente != null ? saldoPendiente : 0.0) + importe;
        if (nuevoSaldo < 0) {
            throw new IllegalArgumentException("El saldo pendiente no puede ser negativo.");
        }
        saldoPendiente = nuevoSaldo;
    }

    }