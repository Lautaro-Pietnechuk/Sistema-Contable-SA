package com.sa.contable.entidades;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Long id_usuario;
    
    @OneToMany(mappedBy = "asiento", fetch = FetchType.EAGER)
    private Set<CuentaAsiento> cuentasAsientos;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Set<CuentaAsiento> getCuentasAsientos() {
        return cuentasAsientos;
    }

    public void setCuentasAsientos(Set<CuentaAsiento> cuentasAsientos) {
        this.cuentasAsientos = cuentasAsientos;
    }

    public void agregarMovimiento(CuentaAsiento movimiento) {
        cuentasAsientos.add(movimiento);
        movimiento.setAsiento(this); // Asegura que el movimiento conozca su asiento
    }
    
}