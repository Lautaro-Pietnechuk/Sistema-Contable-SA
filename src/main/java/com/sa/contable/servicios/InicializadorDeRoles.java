package com.sa.contable.servicios; 


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDeRoles {

    private final UsuarioServicio usuarioServicio;

    public InicializadorDeRoles(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Bean
    public CommandLineRunner asignarRolAdministrador() {
        return args -> {
            Long usuarioId = 1L; // ID del usuario al que quieres asignar el rol
            Long rolId = 1L;     // ID del rol 'Administrador'

            try {
                if (usuarioServicio.existeUsuario(usuarioId)) {
                    usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
                    System.out.println("Rol 'Administrador' asignado exitosamente al usuario.");
                } else {
                    System.out.println("Usuario no encontrado.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
} 
