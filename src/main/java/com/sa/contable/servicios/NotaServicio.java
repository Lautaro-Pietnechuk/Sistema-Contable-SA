package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.DTO.CuentaAsientoDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.Cobro;
import com.sa.contable.entidades.Nota;
import com.sa.contable.entidades.Producto;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.CobroRepositorio;
import com.sa.contable.repositorios.NotaRepositorio;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.ProductoRepositorio;
import com.sa.contable.repositorios.VentaRepositorio;

@Service
public class NotaServicio {

    @Autowired
    private NotaRepositorio notaRepositorio;

    @Autowired
    private ClienteRepository clienteRepositorio;

    @Autowired
    private VentaRepositorio ventasRepositorio;

    @Autowired
    private CobroRepositorio cobroRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private AsientoServicio asientoServicio;

    private static final Logger logger = LoggerFactory.getLogger(NotaServicio.class);

    private static final Long CUENTA_HABER_CREDITO = 121L;
    private static final Long CUENTA_DEBE_CREDITO = 411L;
    private static final Long CUENTA_HABER_DEBITO = 411L;
    private static final Long CUENTA_DEBE_DEBITO = 121L;

    public List<Nota> obtenerTodas() {
        return notaRepositorio.findAll();
    }

    public List<Nota> obtenerPorVenta(Long idVenta) {
        return notaRepositorio.findByIdVenta(idVenta);
    }

    @Transactional
    public Nota crearNota(Nota nota, Long usuarioId) {
        logger.info("Iniciando creación de Nota tipo '{}' para la Venta ID: {}, solicitada por Usuario ID: {}", nota.getTipo(), nota.getIdVenta(), usuarioId);

        // Aseguramos que la fecha sea la actual si no viene en el JSON
        if (nota.getFecha() == null) {
            logger.debug("La fecha de la nota es nula. Asignando fecha actual.");
            nota.setFecha(LocalDate.now());
        }
        Venta venta = ventasRepositorio.findById(nota.getIdVenta())
            .orElseThrow(() -> {
                logger.error("Fallo al crear nota: Venta no encontrada con id {}", nota.getIdVenta());
                return new RuntimeException("Venta no encontrada con id: " + nota.getIdVenta());
            });
        if (venta.getEstado().equals("ANULADA")) {
            logger.warn("Intento de crear nota para una venta ya anulada. Venta ID: {}", nota.getIdVenta());
            throw new IllegalStateException("No se pueden crear notas para una venta anulada.");
        }

        if (nota.getTipo() == 'C') {
            logger.info("Procesando Nota de Crédito (Anulación de venta).");

            Cliente cliente = venta.getCliente();
            double saldoPendiente = venta.getSaldoPendiente();
            double montoNota = nota.getMonto().doubleValue();
            double montoAplicado = Math.min(montoNota, saldoPendiente);
            double saldoAFavorGenerado = montoNota - montoAplicado;

            if (saldoAFavorGenerado > 0) {
                cliente.setSaldoAFavor(cliente.getSaldoAFavor() + saldoAFavorGenerado);
                logger.info("Saldo a favor generado por nota de crédito: clienteId={}, monto={}, saldoAFavor={}",
                        cliente.getId(), saldoAFavorGenerado, cliente.getSaldoAFavor());
            }
            double saldoPendienteClienteAnterior = cliente.getSaldoPendiente();
            cliente.setSaldoPendiente(Math.max(0.0, saldoPendienteClienteAnterior - montoAplicado));

            List<Cobro> cobrosVinculados = cobroRepositorio.findByVentaIdAndMontoAplicadoGreaterThan(venta.getId(), 0.0);
            double montoCobrosARevertir = 0.0;
            for (Cobro cobro : cobrosVinculados) {
                double montoARevertir = cobro.getMontoAplicado();
                if (montoARevertir > 0) {
                    cobro.setMontoAplicado(0.0);
                    cobro.setVenta(null);
                    montoCobrosARevertir += montoARevertir;
                    cobroRepositorio.save(cobro);
                    logger.info("Cobro ID {} desvinculado de la venta anulada. Monto aplicado revertido: {}",
                            cobro.getId(), montoARevertir);
                }
            }
            if (montoCobrosARevertir > 0) {
                cliente.setSaldoAFavor(cliente.getSaldoAFavor() + montoCobrosARevertir);
                logger.info("Saldo a favor restaurado por venta anulada: clienteId={}, monto={}, saldoAFavor={}",
                        cliente.getId(), montoCobrosARevertir, cliente.getSaldoAFavor());
            }

            venta.setSaldoPendiente(saldoPendiente - montoAplicado);
            venta.setEstado("ANULADA");
            ventasRepositorio.saveAndFlush(venta);

            AsientoDTO asientoDTO = new AsientoDTO();
            asientoDTO.setFecha(nota.getFecha());
            asientoDTO.setDescripcion("Contra asiento por nota de Credito para venta ID: " + nota.getIdVenta() + " - Motivo: " + nota.getMotivo());
            asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
        
            // CORRECCIÓN APLICADA: Cruzamos las cuentas para el contrasiento
            CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
            movimientoDebe.setCuentaCodigo(CUENTA_DEBE_CREDITO); // El Debe usa la cuenta de Ventas
            movimientoDebe.setDebe(nota.getMonto());
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
        
            CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
            movimientoHaber.setCuentaCodigo(CUENTA_HABER_CREDITO); // El Haber usa la cuenta de Deudores
            movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
            movimientoHaber.setHaber(nota.getMonto());
        
            asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
        
            logger.debug("Generando contrasiento contable por monto: {}", nota.getMonto());
            asientoServicio.crearAsiento(asientoDTO, usuarioId);
            logger.info("Contrasiento creado exitosamente.");

            logger.debug("Iniciando restauración de stock para los productos de la venta ID: {}", nota.getIdVenta());
            venta.getDetalles().forEach(detalle -> {
                int cantidad = detalle.getCantidad();
                Producto producto = detalle.getProducto();
            
                // logger a nivel TRACE o DEBUG para no inundar la consola si la venta tiene muchos items
                logger.debug("Restaurando {} unidades al producto ID: {} (Stock anterior: {})", cantidad, producto.getId(), producto.getStock());
            
                producto.setStock(producto.getStock() + cantidad);
                productoRepositorio.save(producto);
            });
            logger.info("Stock de la venta ID {} restaurado exitosamente.", nota.getIdVenta());

            clienteRepositorio.saveAndFlush(cliente);
            logger.info("Venta ID {} marcada como anulada en la base de datos.", venta.getId());

        } else if (nota.getTipo() == 'D') {
            logger.info("Procesando Nota de Débito (Ajuste a la venta).");

            Cliente cliente = venta.getCliente();
            double montoNota = nota.getMonto().doubleValue();
            double saldoAFavorActual = cliente.getSaldoAFavor();
            double saldoAFavorUsado = Math.min(saldoAFavorActual, montoNota);
            double saldoPendienteGenerado = montoNota - saldoAFavorUsado;
            if (saldoAFavorUsado > 0) {
                cliente.setSaldoAFavor(saldoAFavorActual - saldoAFavorUsado);
                logger.info("Saldo a favor aplicado por nota de débito: clienteId={}, monto={}, saldoAFavor={}",
                        cliente.getId(), saldoAFavorUsado, cliente.getSaldoAFavor());
            }

            venta.setTotal(venta.getTotal() + montoNota);
            venta.setSaldoPendiente(venta.getSaldoPendiente() + saldoPendienteGenerado);
            ventasRepositorio.save(venta);
            logger.info("Venta actualizada por nota de débito: ventaId={}, total={}, saldoPendiente={}",
                    venta.getId(), venta.getTotal(), venta.getSaldoPendiente());

            cliente.aumentarSaldoPendiente(saldoPendienteGenerado);
            clienteRepositorio.save(cliente);

                AsientoDTO asientoDTO = new AsientoDTO();
                asientoDTO.setFecha(nota.getFecha());
                asientoDTO.setDescripcion("Asiento por nota de Débito para venta ID: " + nota.getIdVenta() + " - Motivo: " + nota.getMotivo());
                asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
            
                CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
                movimientoDebe.setCuentaCodigo(CUENTA_DEBE_DEBITO); // El Debe usa la cuenta de Deudores
                movimientoDebe.setDebe(nota.getMonto());
                movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
            
                CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
                movimientoHaber.setCuentaCodigo(CUENTA_HABER_DEBITO); // El Haber usa la cuenta de Ventas
                movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
                movimientoHaber.setHaber(nota.getMonto());
            
                asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
            
                logger.debug("Generando asiento contable por nota de débito con monto: {}", nota.getMonto());
                asientoServicio.crearAsiento(asientoDTO, usuarioId);
                logger.info("Asiento contable por nota de débito creado exitosamente.");
        }
    
        Nota notaGuardada = notaRepositorio.save(nota);
        logger.info("Transacción completada: Nota de crédito guardada exitosamente con ID: {}", notaGuardada.getIdNota());
    
        return notaGuardada;
    }
}