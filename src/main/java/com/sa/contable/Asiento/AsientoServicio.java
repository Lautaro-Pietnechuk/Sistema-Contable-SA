package com.sa.contable.Asiento;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.Cuenta.CuentaMovimientoDTO;
import com.sa.contable.Relaciones.CuentaAsiento;
import com.sa.contable.Relaciones.CuentaAsientoRepositorio;

import jakarta.transaction.Transactional;

@Service
public class AsientoServicio {

    @Autowired
    private AsientoRepositorio asientoRepositorio;

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    @Transactional
    public Asiento crearAsiento(AsientoDTO asientoDTO) {
        // Validar que el asientoDTO tenga movimientos
        if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().isEmpty()) {
            throw new IllegalArgumentException("El asiento debe contener al menos un movimiento.");
        }

        Asiento asiento = new Asiento();
        asiento.setFecha(asientoDTO.getFecha());
        asiento.setDescripcion(asientoDTO.getDescripcion());

        double totalDebe = 0;
        double totalHaber = 0;

        // Guardar las cuentas asociadas al asiento
        for (CuentaMovimientoDTO cuentaMovimiento : asientoDTO.getMovimientos()) {
            CuentaAsiento cuentaAsiento = new CuentaAsiento();
            cuentaAsiento.setidAsiento(asiento.getId());
            cuentaAsiento.setidCuenta(cuentaMovimiento.getCuenta().getId());
            
            // Manejo de debe y haber en lugar de monto
            if (cuentaMovimiento.getTipo().equalsIgnoreCase("DEBE")) {
                cuentaAsiento.setDebe(cuentaMovimiento.getMonto());
                cuentaAsiento.setHaber(0.0); // Si es debe, el haber es 0
                totalDebe += cuentaMovimiento.getMonto(); // Sumar al total de débitos
            } else if (cuentaMovimiento.getTipo().equalsIgnoreCase("HABER")) {
                cuentaAsiento.setHaber(cuentaMovimiento.getMonto());
                cuentaAsiento.setDebe(0.0); // Si es haber, el debe es 0
                totalHaber += cuentaMovimiento.getMonto(); // Sumar al total de créditos
            }
        
            // Guardar en la base de datos
            cuentaAsientoRepositorio.save(cuentaAsiento);
        }

        // Validar que los débitos sean iguales a los créditos
        if (totalDebe != totalHaber) {
            throw new IllegalArgumentException("Los débitos y créditos no cuadran");
        }

        // Guardar el asiento en la base de datos
        return asientoRepositorio.save(asiento);
    }


    public List<Asiento> listarAsientos() {
        return asientoRepositorio.findAll();
    }
}
