package com.sa.contable.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numeroComprobante;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(nullable = false)
    private Double total;

    @Column(length = 255)
    private String observaciones;
    @Column(nullable = false, length = 20)
    private String tipoDePago = "EFECTIVO";

    // NUEVO CAMPO: Inicializado por defecto en PENDIENTE
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private Double saldoPendiente = 0.0; 

    public Venta() {
        this.fecha = LocalDateTime.now();
    }

    public Venta(String numeroComprobante, Cliente cliente, Double total) {
        this.numeroComprobante = numeroComprobante;
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.total = total;
        this.estado = "PENDIENTE"; // Se asegura de nacer pendiente
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (estado.equals("PENDIENTE") || estado.equals("PAGADA") || estado.equals("ANULADA")) {
            this.estado = estado;
            if (estado.equals("PAGADA")) {
                this.saldoPendiente = 0.0; // Si se paga, el saldo pendiente se vuelve 0
            }
        } else {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }
    }

    public String getTipoDePago() {
        return tipoDePago;
    }

    public void setTipoDePago(String tipoDePago) {
        if (tipoDePago == null || tipoDePago.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de pago no puede ser nulo o vacío");
        }

        String limpio = tipoDePago.trim().toUpperCase();
        if (!limpio.matches("EFECTIVO|DEBITO|CUENTA_CORRIENTE")) {
            throw new IllegalArgumentException("Tipo de pago inválido. Debe ser 'EFECTIVO', 'DEBITO' o 'CUENTA_CORRIENTE'.");
        }
        this.tipoDePago = limpio;
    }

    // Métodos auxiliares
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void eliminarDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }

    public Double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(Double saldoPendiente) {
        if (saldoPendiente < 0) {
            throw new IllegalArgumentException("El saldo pendiente no puede ser negativo.");
        }
        this.saldoPendiente = saldoPendiente;
        if (saldoPendiente == 0) {
            this.estado = "PAGADA"; // Si el saldo pendiente es 0 o negativo, la venta se considera pagada
        } else {
            this.estado = "PENDIENTE"; // Si hay saldo pendiente, la venta sigue siendo pendiente
        }
    }
}
