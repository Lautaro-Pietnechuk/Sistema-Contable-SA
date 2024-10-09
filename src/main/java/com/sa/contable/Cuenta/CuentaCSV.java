package com.sa.contable.Cuenta;

public class CuentaCSV {
    private String idCuenta;
    private String nombre;
    private boolean recibeSaldo;
    private String tipo;

    // Constructor
    public CuentaCSV(String idCuenta, String nombre, boolean recibeSaldo, String tipo) {
        this.idCuenta = idCuenta;
        this.nombre = nombre;
        this.recibeSaldo = recibeSaldo;
        this.tipo = tipo;
    }

    // Getters y Setters
    public String getidCuenta() {
        return idCuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isRecibeSaldo() {
        return recibeSaldo;
    }

    public String getTipo() {
        return tipo;
    }
}
