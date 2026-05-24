package com.sa.contable.entidades;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;


@Entity
public class Cobro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha = LocalDateTime.now();
    private Double monto; // Los $500 que te trajo el cliente

    @ManyToOne
    private Cliente cliente;

    private String metodoPago; // "EFECTIVO", "TRANSFERENCIA"

    @Column(nullable = false)
    private Boolean anulado = false;

    public Cobro() {
    }

    public Cobro(Double monto, Cliente cliente, String metodoPago) {
        this.monto = monto;
        this.cliente = cliente;
        this.metodoPago = metodoPago;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }
    
}