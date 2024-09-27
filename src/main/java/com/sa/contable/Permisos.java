package com.sa.contable;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Permisos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "permissions")
    private Set<Roles> roles;

    // Getters y Setters
}
