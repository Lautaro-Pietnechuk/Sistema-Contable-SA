package com.sa.contable.dto;

import java.time.LocalDate;
import java.util.List;

public class AsientoDTO {
    private LocalDate fecha;
    private String descripcion;
    private Long idUsuario;
    private List<CuentaAsientoDTO> movimientos;
    private Long id;


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

    public List<CuentaAsientoDTO> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<CuentaAsientoDTO> movimientos) {
        this.movimientos = movimientos;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
 