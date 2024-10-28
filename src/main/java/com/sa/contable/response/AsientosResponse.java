package com.sa.contable.response;

import com.sa.contable.dto.AsientoDTO;
import java.util.List;



public class AsientosResponse {
    private List<AsientoDTO> contenido;
    private long totalElementos;

    public AsientosResponse(List<AsientoDTO> contenido, long totalElementos) {
        this.contenido = contenido;
        this.totalElementos = totalElementos;
    }

    public List<AsientoDTO> getContenido() {
        return contenido;
    }

    public void setContenido(List<AsientoDTO> contenido) {
        this.contenido = contenido;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }
}
