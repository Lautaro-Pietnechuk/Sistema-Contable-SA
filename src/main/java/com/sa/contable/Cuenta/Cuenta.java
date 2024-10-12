package com.sa.contable.Cuenta;

import java.util.HashSet;
import java.util.Set;

import com.sa.contable.Relaciones.CuentaAsiento;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private Integer codigo; // Cambiado a Integer

    @ManyToOne // Relación con la cuenta padre
    @JoinColumn(name = "cuenta_padre_id") // Columna que referencia la cuenta padre
    private Cuenta cuentaPadre;

    @OneToMany(mappedBy = "cuentaPadre", cascade = CascadeType.ALL)
    private Set<Cuenta> subCuentas = new HashSet<>();

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean recibeSaldo; // Solo las hojas reciben saldo

    @Column(nullable = false)
    private Long saldoActual = 0L;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL)
    private Set<CuentaAsiento> cuentasAsientos = new HashSet<>(); // Inicialización

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCodigo() { // Cambiado a Integer
        return codigo;
    }

    public void setCodigo(Integer codigo) { // Cambiado a Integer
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getRecibeSaldo() {
        return recibeSaldo;
    }

    public void setRecibeSaldo(Boolean recibeSaldo) {
        this.recibeSaldo = recibeSaldo;
    }

    public Long getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(Long saldoActual) {
        this.saldoActual = saldoActual;
    }

    public Set<CuentaAsiento> getCuentasAsientos() {
        return cuentasAsientos;
    }

    public void setCuentasAsientos(Set<CuentaAsiento> cuentasAsientos) {
        this.cuentasAsientos = cuentasAsientos;
    }

    public Cuenta getCuentaPadre() {
        return cuentaPadre;
    }

    public void setCuentaPadre(Cuenta cuentaPadre) {
        this.cuentaPadre = cuentaPadre;
    }

    public Set<Cuenta> getSubCuentas() {
        return subCuentas;
    }

    public void setSubCuentas(Set<Cuenta> subCuentas) {
        this.subCuentas = subCuentas;
    }

    // Métodos para agregar y eliminar subcuentas
    public void agregarSubCuenta(Cuenta subCuenta) {
        subCuentas.add(subCuenta);
        subCuenta.setCuentaPadre(this);
    }

    public void eliminarSubCuenta(Cuenta subCuenta) {
        subCuentas.remove(subCuenta);
        subCuenta.setCuentaPadre(null);
    }
}
