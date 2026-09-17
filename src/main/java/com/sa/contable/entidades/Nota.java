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

    @Column(name = "numero_comprobante", nullable = false, length = 50)
    private String numeroComprobante;

    @Column(name = "tipo", nullable = false, length = 1)
    private char tipo; // char(1) NN (C para Crédito, D para Débito)

    @Column(name = "id_venta", nullable = false)
    private Long idVenta; // bigint NN 

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha; // date NN 

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto; // numeric(15,2) NN 

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo; // text NN 

    @Column(name = "tipo_de_pago", length = 20)
    private String tipoDePago = "CUENTA_CORRIENTE";

    // Getters y Setters

    public Long getIdNota() {
        return idNota;
    }

    public void setIdNota(Long idNota) {
        this.idNota = idNota;
    }

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        if (tipo != 'C' && tipo != 'D') {
            throw new IllegalArgumentException("El tipo de nota debe ser 'C' para Crédito o 'D' para Débito.");
        } else {
            this.tipo = tipo;
        }   
        
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

    public String getTipoDePago() {
        return tipoDePago;
    }

    public void setTipoDePago(String tipoDePago) {
        if (tipoDePago == null || tipoDePago.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de pago no puede ser nulo o vacío");
        }

        String limpio = tipoDePago.trim().toUpperCase();
        if (!limpio.matches("EFECTIVO|DEBITO|TRANSFERENCIA|CUENTA_CORRIENTE")) {
            throw new IllegalArgumentException("Tipo de pago inválido.");
        }
        this.tipoDePago = limpio;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    

}