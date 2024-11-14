package com.sa.contable.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Cuenta;


@Repository
public interface CuentaRepositorio extends JpaRepository<Cuenta, Long> {

    // Buscar cuentas hijas activas de una cuenta padre específica
    List<Cuenta> findByCuentaPadreAndEliminadaFalse(Cuenta cuentaPadre);

    // Buscar cuentas activas que reciben saldo
    @EntityGraph(attributePaths = {"cuentasAsientos"})
    List<Cuenta> findByRecibeSaldoAndEliminadaFalse(boolean recibeSaldo);

    // Obtener una cuenta activa por código
    Cuenta findByCodigoAndEliminadaFalse(Long codigo);

    // Buscar cuentas hijas activas de un padre por su código
    List<Cuenta> findByCuentaPadre_CodigoAndEliminadaFalse(Long codigo);

    // Listar solo las cuentas raíz activas (cuentas sin padre y no eliminadas)
    List<Cuenta> findByCuentaPadreIsNullAndEliminadaFalse();
}
