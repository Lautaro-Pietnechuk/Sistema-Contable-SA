/* package com.sa.contable.noenuso;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sa.contable.entidades.Cuenta;
import com.sa.contable.servicios.CuentaServicio;

import jakarta.annotation.PostConstruct;

@Component
public class CuentaInitializer {

    private final CuentaServicio cuentaServicio;

    @Autowired
    public CuentaInitializer(CuentaServicio cuentaServicio) {
        this.cuentaServicio = cuentaServicio;
    }

    @PostConstruct
    public void init() {
        crearCuentas();
    }

    public void crearCuentas() {
        // Datos de las cuentas
        List<Object[]> cuentasData = Arrays.asList(
            new Object[]{100L, "Activo", false, "Activo"},
            new Object[]{110L, "Caja y Bancos", false, "Activo"},
            new Object[]{111L, "Caja", true, "Activo"},
            new Object[]{112L, "Banco plazo fijo", true, "Activo"},
            new Object[]{113L, "Banco c/c", true, "Activo"},
            new Object[]{120L, "Creditos", false, "Activo"},
            new Object[]{121L, "Deudores por Ventas", true, "Activo"},
            new Object[]{122L, "Documentos a Cobrar", true, "Activo"},
            new Object[]{123L, "Valores a Depositar", true, "Activo"},
            new Object[]{130L, "Bienes de cambio", false, "Activo"},
            new Object[]{131L, "Mercaderias", true, "Activo"},
            new Object[]{140L, "Bienes de uso", false, "Activo"},
            new Object[]{141L, "Inmuebles", true, "Activo"},
            new Object[]{142L, "Rodados", true, "Activo"},
            new Object[]{143L, "Instalaciones", true, "Activo"},
            new Object[]{200L, "Pasivo", false, "Pasivo"},
            new Object[]{210L, "Deudas Comerciales", false, "Pasivo"},
            new Object[]{211L, "Proveedores", true, "Pasivo"},
            new Object[]{212L, "Sueldos a Pagar", true, "Pasivo"},
            new Object[]{220L, "Deudas Fiscales", false, "Pasivo"},
            new Object[]{221L, "Impuestos a Pagar", true, "Pasivo"},
            new Object[]{222L, "Moratorias", true, "Pasivo"},
            new Object[]{230L, "Prestamos Bancarios", true, "Pasivo"},
            new Object[]{300L, "Patrimonio", false, "Patrimonio"},
            new Object[]{310L, "Capital", true, "Patrimonio"},
            new Object[]{320L, "Resultados", true, "Patrimonio"},
            new Object[]{400L, "Ingresos", false, "Ingreso"},
            new Object[]{410L, "Ventas", false, "Ingreso"},
            new Object[]{411L, "Ventas", true, "Ingreso"},
            new Object[]{420L, "Otros ingresos", false, "Ingreso"},
            new Object[]{430L, "Intereses Ganados", true, "Ingreso"},
            new Object[]{500L, "Egresos", false, "Egreso"},
            new Object[]{510L, "Costo de Mercader�a Vendida", true, "Egreso"},
            new Object[]{520L, "Impuestos", true, "Egreso"},
            new Object[]{530L, "Sueldos", true, "Egreso"},
            new Object[]{540L, "Intereses", true, "Egreso"},
            new Object[]{550L, "Alquileres", true, "Egreso"}
        );

        // Crear y guardar las cuentas
        for (Object[] data : cuentasData) {
            long codigo = (Long) data[0];
            String nombre = (String) data[1];
            boolean recibeSaldo = (Boolean) data[2];
            String tipo = (String) data[3];

            // Asignar saldo actual a 0
            BigDecimal saldoActual = BigDecimal.ZERO;

            Cuenta cuenta = new Cuenta(codigo, nombre, recibeSaldo, tipo, saldoActual);
            
            // Asignar cuenta padre si corresponde
            asignarCuentaPadre(cuenta);
            
            cuentaServicio.crearCuenta(cuenta);
        }

        System.out.println("Cuentas creadas correctamente.");
    }

    private void asignarCuentaPadre(Cuenta cuenta) {
        long codigo = cuenta.getCodigo();
        Long codigoPadre = null;
    
        String codigoStr = String.valueOf(codigo); // Convertir el código a String
    
        // Verificar si el código es de una cuenta principal
        if (codigoStr.length() == 3 && codigoStr.endsWith("00")) {
            return; // No tiene padre
        }
    
        // Determinar el código de la cuenta padre
        switch (codigoStr) {
            case "110":
            case "120":
            case "130":
            case "140":
                codigoPadre = 100L; // 110, 120, 130 y 140 son hijas de 100
                break;
            case "210":
            case "220":
            case "230":
                codigoPadre = 200L; // 210, 220 y 230 son hijas de 200
                break;
            case "310":
            case "320":
            case "330":
                codigoPadre = 300L; // 310, 320 y 330 son hijas de 300
                break;
            case "410":
            case "420":
            case "430":
                codigoPadre = 400L; // 410 y 420 son hijas de 400
                break;
            case "510":
            case "520":
            case "530":
            case "540":
            case "550":
                codigoPadre = 500L; // 510, 520, 530, 540 y 550 son hijas de 500
                break;
            default:
                if (codigoStr.length() == 3) {
                    codigoPadre = Long.parseLong(codigoStr.substring(0, 2) + "0");
                } else if (codigoStr.length() == 2) {
                    codigoPadre = Long.parseLong(codigoStr.charAt(0) + "00");
                }
                break;
        }
    
        // Buscar la cuenta padre en la base de datos
        if (codigoPadre != null) {
            Optional<Cuenta> cuentaPadreOpt = cuentaServicio.obtenerCuentaPorCodigo(codigoPadre);
            cuentaPadreOpt.ifPresent(cuentaPadre -> {
                cuenta.setCuentaPadre(cuentaPadre);
                cuentaPadre.agregarHija(cuenta);
                cuentaPadre.setRecibeSaldo(false); // No recibe saldo si tiene hijas
            });
        }
    }
    
    
    
}
  */