package com.sa.contable.entidades;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Cuenta {

    @Id
    @Column(nullable = false, unique = true)
    private Long codigo; // Usar codigo como ID

    @Column(nullable = false)
    private String nombre;

    @ManyToOne // Relación con la cuenta padre
    @JoinColumn(name = "cuenta_padre_codigo") // Columna que referencia la cuenta padre por código
    private Cuenta cuentaPadre;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cuentaPadre")
    private Set<Cuenta> Hijas = new HashSet<>();

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean recibeSaldo; // Solo las hojas reciben saldo

    @Column(nullable = false)
    private BigDecimal saldoActual = BigDecimal.ZERO; // Cambiado a BigDecimal

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CuentaAsiento> cuentasAsientos = new HashSet<>(); // Inicialización

    // Constructores
    public Cuenta() {}

    // Getters y Setters
    public Long getCodigo() {
        return codigo; // Ahora este es el ID
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public BigDecimal getSaldoActual() {
        return saldoActual; // Cambiado a BigDecimal
    }

    public void setSaldoActual(BigDecimal saldoActual) { // Cambiado a BigDecimal
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

    public Set<Cuenta> getHijas() {
        return Hijas;
    }

    public void setHijas(Set<Cuenta> subCuentas) {
        this.Hijas = subCuentas;
    }

    // Métodos para agregar y eliminar subcuentas
    public void agregarHija(Cuenta hija) {
        Hijas.add(hija);
        hija.setCuentaPadre(this);
    }

    public void eliminarHija(Cuenta hija) {
        Hijas.remove(hija);
        hija.setCuentaPadre(null);
    }
}
