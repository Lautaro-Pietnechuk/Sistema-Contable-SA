package com.sa.contable.dto;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String rol;  // Ejemplo: nombre del rol asignado.

    public UsuarioDTO(Long id, String nombre, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
    }

    // Getters y setters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
}
