package com.sa.contable.DTO;

public class ClienteDTO {
    private Long id;
    private String nombre;
    private Double saldoPendiente;

    public ClienteDTO() {}

    public ClienteDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public ClienteDTO(Long id, String nombre, Double saldoPendiente) {
        this.id = id;
        this.nombre = nombre;
        this.saldoPendiente = saldoPendiente;
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

    public Double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(Double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }
    
}
