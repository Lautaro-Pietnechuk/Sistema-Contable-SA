package com.sa.contable.repositorios;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sa.contable.entidades.CuentaAsiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CuentaAsientoRepositorio extends JpaRepository<CuentaAsiento, Long> {

    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN TRUE ELSE FALSE END FROM CuentaAsiento ca WHERE ca.cuenta.codigo = :cuentaCodigo")
    boolean existsByCuentaCodigo(@Param("cuentaCodigo") Long cuentaCodigo);

    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN TRUE ELSE FALSE END FROM CuentaAsiento ca WHERE ca.asiento.id = :asientoId")
    boolean existsByAsientoId(@Param("asientoId") Long asientoId);

    @Query("SELECT ca FROM CuentaAsiento ca WHERE ca.cuenta.codigo = :codigoCuenta AND ca.asiento.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY ca.asiento.fecha")
    List<CuentaAsiento> findByCuentaAndFechaBetween(@Param("codigoCuenta") String codigoCuenta, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}
