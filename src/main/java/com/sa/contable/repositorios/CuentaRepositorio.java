package com.sa.contable.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sa.contable.entidades.Cuenta;

public interface CuentaRepositorio extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByCuentaPadre(Cuenta cuentaPadre);
    // Puedes agregar métodos personalizados aquí si es necesario

    @EntityGraph(attributePaths = {"cuentasAsientos"})
    List<Cuenta> findByRecibeSaldo(boolean recibeSaldo);

    Cuenta findByCodigo(Long codigo);

    List<Cuenta> findByCuentaPadre_Codigo(Long codigo);

    List<Cuenta> findByCuentaPadreIsNull();

}
