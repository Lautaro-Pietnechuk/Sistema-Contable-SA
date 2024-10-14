package com.sa.contable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sa.contable.Asiento.Asiento;
import com.sa.contable.Asiento.AsientoRepositorio;
import com.sa.contable.Asiento.AsientoServicio;
import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.DTO.CuentaMovimientoDTO;
import com.sa.contable.Relaciones.CuentaAsiento;
import com.sa.contable.Relaciones.CuentaAsientoRepositorio;
import com.sa.contable.cuenta.Cuenta;

@ExtendWith(MockitoExtension.class)
public class AsientoServiceTest {

    @Mock
    private AsientoRepositorio asientoRepositorio;

    @Mock
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    @InjectMocks
    private AsientoServicio asientoServicio;

    @Test
    public void testCrearAsiento_conAsientoValido_debeGuardar() {
        // Datos del asiento DTO
        AsientoDTO asientoDTO = new AsientoDTO();
        asientoDTO.setFecha(java.sql.Date.valueOf(LocalDate.now()));
        asientoDTO.setDescripcion("Asiento de prueba");

        // Movimientos (uno de debe y uno de haber)
        List<CuentaMovimientoDTO> movimientos = new ArrayList<>();
        movimientos.add(new CuentaMovimientoDTO(new Cuenta(), 100.0, "DEBE"));
        movimientos.add(new CuentaMovimientoDTO(new Cuenta(), 100.0, "HABER"));

        asientoDTO.setMovimientos(movimientos);

        // Ejecutar el método a probar
        asientoServicio.crearAsiento(asientoDTO);

        // Verificar que se haya guardado correctamente
        verify(asientoRepositorio, times(1)).save(any(Asiento.class));

        verify(cuentaAsientoRepositorio, times(2)).save(any(CuentaAsiento.class));
    }

    @Test
    public void testCrearAsiento_conDiferenciaDebeHaber_debeLanzarExcepcion() {
        // Datos del asiento DTO
        AsientoDTO asientoDTO = new AsientoDTO();
        asientoDTO.setFecha(java.sql.Date.valueOf(LocalDate.now()));
        asientoDTO.setDescripcion("Asiento desbalanceado");

        // Movimientos (débito de 100, crédito de 50)
        List<CuentaMovimientoDTO> movimientos = new ArrayList<>();
        movimientos.add(new CuentaMovimientoDTO(new Cuenta(), 100.0, "DEBE"));
        movimientos.add(new CuentaMovimientoDTO(new Cuenta(), 50.0, "HABER"));

        asientoDTO.setMovimientos(movimientos);

        // Verificar que se lanza la excepción por desbalance
        assertThrows(IllegalArgumentException.class, () -> {
            asientoServicio.crearAsiento(asientoDTO);
        });

    }

}
