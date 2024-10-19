package com.sa.contable.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cuenta_codigo;
    private String tipo; // 'debe' o 'haber'
    private Double monto;

    @ManyToOne // Relación con Asiento
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento;

    // Getters y Setters
}
