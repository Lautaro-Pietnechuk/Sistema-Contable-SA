package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.sa.contable.Rol.Rol;
import com.sa.contable.Rol.RolControlador;

@SpringBootApplication
public class ContableApplication {

    @Autowired
    private RolControlador rolControlador;

    public static void main(String[] args) {
        SpringApplication.run(ContableApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return (args) -> {
            Rol usuario = new Rol("Usuario");
            Rol administrador = new Rol("Administrador");
            rolControlador.crearRol(usuario);
            rolControlador.crearRol(administrador);
        };
    }
}