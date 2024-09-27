package com.sa.contable;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "permisos")
    private Set<Rol> roles;

    // Getters y Setters
}
