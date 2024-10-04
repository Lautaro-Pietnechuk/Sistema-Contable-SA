package com.sa.contable.Modelo_Asientos;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean recibeSaldo;

    @Column(nullable = false)
    private Long saldoActual;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL)
    private Set<CuentaAsiento> cuentasAsientos;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getRecibeSaldo() {
        return recibeSaldo;
    }

    public void setRecibeSaldo(Boolean recibeSaldo) {
        this.recibeSaldo = recibeSaldo;
    }

    public Long getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(Long saldoActual) {
        this.saldoActual = saldoActual;
    }

    public Set<CuentaAsiento> getCuentasAsientos() {
        return cuentasAsientos;
    }

    public void setCuentasAsientos(Set<CuentaAsiento> cuentasAsientos) {
        this.cuentasAsientos = cuentasAsientos;
    }
}