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

    @Column
    private Double saldoAFavor = 0.0;

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

    public Double getSaldoAFavor() {
        return saldoAFavor != null ? saldoAFavor : 0.0;
    }

    public void setSaldoAFavor(Double saldoAFavor) {
        if (saldoAFavor == null || saldoAFavor < 0) {
            throw new IllegalArgumentException("El saldo a favor no puede ser nulo ni negativo.");
        }
        this.saldoAFavor = saldoAFavor;
    }


    } 