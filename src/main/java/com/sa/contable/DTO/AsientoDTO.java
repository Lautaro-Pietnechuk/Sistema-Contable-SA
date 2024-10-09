package com.sa.contable.DTO;

import java.util.Date;
import java.util.List;

public class AsientoDTO {
    private Date fecha;
    private String descripcion;
    private List<CuentaMovimientoDTO> movimientos;

    // Getters y Setters
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<CuentaMovimientoDTO> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<CuentaMovimientoDTO> movimientos) {
        this.movimientos = movimientos;
    }
}
 