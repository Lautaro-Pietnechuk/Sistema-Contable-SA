package com.sa.contable.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota", nullable = false)
    private Long idNota; // bigint NN 

    @Column(name = "tipo", nullable = false, length = 1)
    private String tipo; // character NN (Ej: 'C' para Crédito, 'D' para Débito) 


    @Column(name = "id_venta", nullable = false)
    private Long idVenta; // bigint NN 

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha; // date NN 

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto; // numeric(15,2) NN 

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo; // text NN 

    @Column(name = "cuenta_codigo", nullable = false)
    private Long cuentaCodigo; // bigint NN 

    // Getters y Setters

    public Long getIdNota() {
        return idNota;
    }

    public void setIdNota(Long idNota) {
        this.idNota = idNota;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Long getCuentaCodigo() {
        return cuentaCodigo;
    }

    public void setCuentaCodigo(Long cuentaCodigo) {
        this.cuentaCodigo = cuentaCodigo;
    }


}