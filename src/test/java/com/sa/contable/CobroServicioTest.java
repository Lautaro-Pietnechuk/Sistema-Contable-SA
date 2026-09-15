package com.sa.contable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.Cobro;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.CobroRepositorio;
import com.sa.contable.repositorios.VentaRepositorio;
import com.sa.contable.servicios.AsientoServicio;
import com.sa.contable.servicios.CobroServicio;

@ExtendWith(MockitoExtension.class)
public class CobroServicioTest {

    @InjectMocks
    private CobroServicio cobroServicio;

    @Mock
    private CobroRepositorio cobroRepositorio;

    @Mock
    private VentaRepositorio ventaRepositorio;

    @Mock
    private ClienteRepository clienteRepositorio;

    @Mock
    private AsientoServicio asientoServicio;

    @Test
    void anularCobroDebeRestaurarSaldoAFavorYSaldoPendienteDelCliente() throws Exception {
        Cliente cliente = new Cliente();
        setId(cliente, 1L);
        cliente.setNombre("Cliente prueba");
        cliente.setSaldoAFavor(50.0);
        cliente.setSaldoPendiente(100.0);

        Cobro cobro = new Cobro();
        setId(cobro, 1L);
        cobro.setMonto(120.0);
        cobro.setCliente(cliente);
        cobro.setMetodoPago("EFECTIVO");
        cobro.setMontoAplicado(90.0);
        cobro.setSaldoAFavorGenerado(30.0);
        cobro.setAnulado(false);

        Venta venta = new Venta();
        setId(venta, 10L);
        venta.setCliente(cliente);
        venta.setNumeroComprobante("FAC-001");
        venta.setTotal(100.0);
        venta.setTipoDePago("CUENTA_CORRIENTE");
        venta.setEstado("PAGADA");
        venta.setSaldoPendiente(0.0);

        cobro.setVenta(venta);

        when(cobroRepositorio.findById(1L)).thenReturn(Optional.of(cobro));
        when(clienteRepositorio.saveAndFlush(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cobroRepositorio.save(any(Cobro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ventaRepositorio.saveAndFlush(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cobroServicio.anularCobro(1L, "Cobro incorrecto", 1L);

        assertEquals(20.0, cliente.getSaldoAFavor());
        assertEquals(190.0, cliente.getSaldoPendiente());
        assertEquals("PENDIENTE", venta.getEstado());
        assertEquals(90.0, venta.getSaldoPendiente());
        verify(asientoServicio).crearAsiento(any(AsientoDTO.class), eq(1L));
    }

    @Test
    void registrarCobroDebeCrearUnCobroPorVentaYOtroPorSaldoAFavor() throws Exception {
        Cliente cliente = new Cliente();
        setId(cliente, 1L);
        cliente.setNombre("Cliente prueba");
        cliente.setSaldoAFavor(0.0);
        cliente.setSaldoPendiente(4000.0);

        Venta primeraVenta = crearVenta(cliente, 10L, 2000.0, "FAC-001");
        Venta segundaVenta = crearVenta(cliente, 20L, 2000.0, "FAC-002");

        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(ventaRepositorio.findById(10L)).thenReturn(Optional.of(primeraVenta));
        when(ventaRepositorio.findByClienteIdAndEstadoOrderByFechaAsc(1L, "PENDIENTE"))
                .thenReturn(List.of(primeraVenta, segundaVenta));
        when(cobroRepositorio.save(any(Cobro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Cobro> cobros = cobroServicio.registrarCobro(
                1L, 5000.0, "EFECTIVO", 10L, 1L);

        assertEquals(3, cobros.size());
        assertEquals(0.0, primeraVenta.getSaldoPendiente());
        assertEquals(0.0, segundaVenta.getSaldoPendiente());
        assertEquals(1000.0, cliente.getSaldoAFavor());
        assertEquals(0.0, cliente.getSaldoPendiente());

        ArgumentCaptor<Cobro> captor = ArgumentCaptor.forClass(Cobro.class);
        verify(cobroRepositorio, org.mockito.Mockito.times(3)).save(captor.capture());
        List<Cobro> cobrosGuardados = captor.getAllValues();

        assertEquals(2000.0, cobrosGuardados.get(0).getMonto());
        assertEquals(primeraVenta, cobrosGuardados.get(0).getVenta());
        assertEquals(2000.0, cobrosGuardados.get(1).getMonto());
        assertEquals(segundaVenta, cobrosGuardados.get(1).getVenta());
        assertEquals(1000.0, cobrosGuardados.get(2).getMonto());
        assertEquals(0.0, cobrosGuardados.get(2).getMontoAplicado());
        assertEquals(1000.0, cobrosGuardados.get(2).getSaldoAFavorGenerado());
        assertEquals(null, cobrosGuardados.get(2).getVenta());
    }

    private Venta crearVenta(Cliente cliente, Long id, Double saldoPendiente, String comprobante)
            throws Exception {
        Venta venta = new Venta();
        setId(venta, id);
        venta.setCliente(cliente);
        venta.setNumeroComprobante(comprobante);
        venta.setTotal(saldoPendiente);
        venta.setTipoDePago("CUENTA_CORRIENTE");
        venta.setSaldoPendiente(saldoPendiente);
        return venta;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
