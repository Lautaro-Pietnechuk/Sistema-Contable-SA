package com.sa.contable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sa.contable.cuenta.Cuenta;
import com.sa.contable.cuenta.CuentaRepositorio;
import com.sa.contable.cuenta.CuentaServicio;

@ExtendWith(MockitoExtension.class)
public class CuentaServicioTest {

    @InjectMocks
    private CuentaServicio cuentaServicio;

    @Mock
    private CuentaRepositorio cuentaRepositorio;

    @Test
    public void testCrearCuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNombre("Test Cuenta");
        cuenta.setTipo("Activo");
        cuenta.setRecibeSaldo(true);
        cuenta.setSaldoActual(0L);

        when(cuentaRepositorio.save(any(Cuenta.class))).thenReturn(cuenta);

        Cuenta nuevaCuenta = cuentaServicio.crearCuenta(cuenta);
        assertNotNull(nuevaCuenta);
        assertEquals("Test Cuenta", nuevaCuenta.getNombre());
        verify(cuentaRepositorio, times(1)).save(cuenta);
    }

    // Otros métodos de prueba...
}
