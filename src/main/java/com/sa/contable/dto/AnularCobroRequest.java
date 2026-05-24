package com.sa.contable.dto;

public class AnularCobroRequest {
    private String motivo;

    public AnularCobroRequest() {
    }

    public AnularCobroRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
