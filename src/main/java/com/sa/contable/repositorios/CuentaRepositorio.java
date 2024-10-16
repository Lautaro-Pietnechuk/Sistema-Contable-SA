package com.sa.contable.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sa.contable.entidades.Cuenta;

public interface CuentaRepositorio extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByCuentaPadre(Cuenta cuentaPadre);
    // Puedes agregar métodos personalizados aquí si es necesario
}
