/* package com.sa.contable.rol;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.sa.contable.usuario.UsuarioServicio;

@Component
public class InicializadorDeRoles {

    private final UsuarioServicio usuarioServicio;

    public InicializadorDeRoles(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Bean
    public CommandLineRunner asignarRolAdministrador() {
        return args -> {
            Long usuarioId = 7L; // ID del usuario al que quieres asignar el rol
            Long rolId = 7L;     // ID del rol 'Administrador'

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
} */
