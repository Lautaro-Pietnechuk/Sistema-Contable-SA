package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sa.contable.rol.RolControlador;

@SpringBootApplication
public class ContableApplication {

    @Autowired
    private RolControlador rolControlador;

    public static void main(String[] args) {
        SpringApplication.run(ContableApplication.class, args);
    }

}