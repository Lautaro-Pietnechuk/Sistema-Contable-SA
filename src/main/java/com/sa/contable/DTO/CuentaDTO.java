package com.sa.contable.dto;

import java.math.BigDecimal;
import java.util.List;

public class CuentaDTO {

    private Long id;
    private String nombre;
    private Long codigo; // Cambiado a Long
    private String tipo;
    private Boolean recibeSaldo; 
    private BigDecimal saldoActual; // Cambiado a BigDecimal
    private List<CuentaDTO> subCuentas;
    
    // Constructor
    public CuentaDTO(Long id, String nombre, Long codigo, String tipo, Boolean recibeSaldo, BigDecimal saldoActual, List<CuentaDTO> subCuentas) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.tipo = tipo;
        this.recibeSaldo = recibeSaldo;
        this.saldoActual = saldoActual;
        this.subCuentas = subCuentas;
    }

    public CuentaDTO() {
    }

    // Getters y Setters
    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
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

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

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

    public List<CuentaDTO> getSubCuentas() {
        return subCuentas;
    }

    public void setSubCuentas(List<CuentaDTO> subCuentas) {
        this.subCuentas = subCuentas;
    }

    @Override
    public String toString() {
        return "CuentaDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", codigo=" + codigo +
                ", tipo='" + tipo + '\'' +
                ", recibeSaldo=" + recibeSaldo +
                ", saldoActual=" + saldoActual +
                ", subCuentas=" + subCuentas +
                '}';
    }
}
