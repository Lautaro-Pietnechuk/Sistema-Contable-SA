package com.sa.contable.cuenta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepositorio extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByCuentaPadre(Cuenta cuentaPadre);
    // Puedes agregar métodos personalizados aquí si es necesario
}
