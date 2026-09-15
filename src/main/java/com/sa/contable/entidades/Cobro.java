package com.sa.contable.entidades;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Cobro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha = LocalDateTime.now();
    private Double monto; // Los $500 que te trajo el cliente

    @Column // Monto que se aplicó a las ventas pendientes del cliente. Si el cliente trajo $500 y tenía $400 de deuda, este campo será $400 y el otro $100 se guardará como saldo a favor.
    private Double montoAplicado = 0.0;

    @Column // Saldo a favor generado por este cobro. Si el cliente trajo $500 y tenía $400 de deuda, este campo será $100 y el otro $400 se aplicará a la deuda.
    private Double saldoAFavorGenerado = 0.0;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

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

    public Double getMontoAplicado() {
        // Los cobros creados antes de este campo se consideran totalmente imputados.
        return montoAplicado != null ? montoAplicado : (monto != null ? monto : 0.0);
    }

    public void setMontoAplicado(Double montoAplicado) {
        this.montoAplicado = montoAplicado;
    }

    public Double getSaldoAFavorGenerado() {
        return saldoAFavorGenerado != null ? saldoAFavorGenerado : 0.0;
    }

    public void setSaldoAFavorGenerado(Double saldoAFavorGenerado) {
        this.saldoAFavorGenerado = saldoAFavorGenerado;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
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