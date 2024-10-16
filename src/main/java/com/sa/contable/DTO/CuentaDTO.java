package com.sa.contable.dto;

import java.util.List;

public class CuentaDTO {

    private Long id;
    private String nombre;
    private Long codigo; // Cambiado a Long
    private String tipo;
    private Boolean recibeSaldo; 
    private Long saldoActual; 
    private List<CuentaDTO> subCuentas;
    
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

    public Long getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(Long saldoActual) {
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
}
