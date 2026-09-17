package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import com.sa.contable.repositorios.CuentaRepositorio;

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

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    private static final Logger logger = LoggerFactory.getLogger(NotaServicio.class);

    // Constantes de cuentas contables renombradas para mayor claridad
    private static final Long CUENTA_DEUDORES_VENTAS = 121L;
    private static final Long CUENTA_VENTAS = 411L;
    private static final Long CUENTA_CAJA = 111L;
    private static final Long CUENTA_BANCO = 113L;

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
        
        final Long idVenta = nota.getIdVenta();
        Venta venta = ventasRepositorio.findById(idVenta)
            .orElseThrow(() -> {
                logger.error("Fallo al crear nota: Venta no encontrada con id {}", idVenta);
                return new RuntimeException("Venta no encontrada con id: " + idVenta);
            });

        if (nota.getTipoDePago() == null || nota.getTipoDePago().isBlank()) {
            nota.setTipoDePago(venta.getTipoDePago());
        }
            
        if (venta.getEstado().equals("ANULADA")) {
            logger.warn("Intento de crear nota para una venta ya anulada. Venta ID: {}", nota.getIdVenta());
            throw new IllegalStateException("No se pueden crear notas para una venta anulada.");
        }

        // ==========================================
        // GENERACIÓN DE NÚMERO DE COMPROBANTE
        // ==========================================
        nota.setNumeroComprobante("TEMP");
        // Guardamos en la base de datos para generar el idNota autoincremental
        nota = notaRepositorio.save(nota); 
        
        // Creamos el comprobante real: ND-00001 (Débito) o NC-00001 (Crédito)
        String prefijo = (nota.getTipo() == 'D') ? "ND" : "NC";
        String comprobanteReal = prefijo + "-" + String.format("%05d", nota.getIdNota());
        nota.setNumeroComprobante(comprobanteReal);
        // ==========================================

        if (nota.getTipo() == 'C') {
            logger.info("Procesando Nota de Crédito (Anulación de venta).");

            Cliente cliente = venta.getCliente();
            double saldoPendiente = venta.getSaldoPendiente();
            double montoNota = nota.getMonto().doubleValue();
            double montoAplicado = Math.min(montoNota, saldoPendiente);
            double importePagado = Math.max(0.0, venta.getTotal() - saldoPendiente);
            double saldoAFavorGenerado = "CUENTA_CORRIENTE".equals(nota.getTipoDePago())
                    ? Math.min(importePagado, montoNota - montoAplicado)
                    : 0.0;

            if (importePagado > 0 && "EFECTIVO".equals(nota.getTipoDePago())) {
                BigDecimal saldoCaja = cuentaRepositorio.findById(CUENTA_CAJA)
                        .orElseThrow(() -> new IllegalStateException("No existe la cuenta Caja (111)."))
                        .getSaldoActual();
                if (saldoCaja == null || saldoCaja.compareTo(BigDecimal.valueOf(importePagado)) < 0) {
                    throw new IllegalStateException("La cuenta Caja (111) no tiene saldo suficiente para devolver $"
                            + importePagado + ".");
                }
            }

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
                logger.info("Cobros revertidos por venta anulada: clienteId={}, monto={}",
                    cliente.getId(), montoCobrosARevertir);
            }

            venta.setSaldoPendiente(saldoPendiente - montoAplicado);
            venta.setEstado("ANULADA");
            ventasRepositorio.saveAndFlush(venta);

            AsientoDTO asientoDTO = new AsientoDTO();
            asientoDTO.setFecha(nota.getFecha());
            asientoDTO.setDescripcion("Nota de Credito para venta ID: " + nota.getIdVenta() + " - Motivo: " + nota.getMotivo());
            asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
        
            CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
            movimientoDebe.setCuentaCodigo(CUENTA_VENTAS); // El Debe usa la cuenta de Ventas
            movimientoDebe.setDebe(nota.getMonto());
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
        
            List<CuentaAsientoDTO> movimientos = new ArrayList<>();
            movimientos.add(movimientoDebe);

            double importeNoPagado = Math.max(0.0, montoNota - importePagado);
            if (importeNoPagado > 0) {
                CuentaAsientoDTO movimientoHaberDeudores = new CuentaAsientoDTO();
                movimientoHaberDeudores.setCuentaCodigo(CUENTA_DEUDORES_VENTAS);
                movimientoHaberDeudores.setDebe(BigDecimal.ZERO);
                movimientoHaberDeudores.setHaber(BigDecimal.valueOf(importeNoPagado));
                movimientos.add(movimientoHaberDeudores);
            }
            if (importePagado > 0) {
                CuentaAsientoDTO movimientoHaberDevolucion = new CuentaAsientoDTO();
                movimientoHaberDevolucion.setCuentaCodigo("EFECTIVO".equals(nota.getTipoDePago())
                        ? CUENTA_CAJA : CUENTA_DEUDORES_VENTAS);
                movimientoHaberDevolucion.setDebe(BigDecimal.ZERO);
                movimientoHaberDevolucion.setHaber(BigDecimal.valueOf(importePagado));
                movimientos.add(movimientoHaberDevolucion);
            }

            asientoDTO.setMovimientos(movimientos);
        
            logger.debug("Generando contrasiento contable por monto: {}", nota.getMonto());
            asientoServicio.crearAsiento(asientoDTO, usuarioId);
            logger.info("Contrasiento creado exitosamente.");

            logger.debug("Iniciando restauración de stock para los productos de la venta ID: {}", nota.getIdVenta());
            venta.getDetalles().forEach(detalle -> {
                int cantidad = detalle.getCantidad();
                Producto producto = detalle.getProducto();
            
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
            boolean notaEnCuentaCorriente = "CUENTA_CORRIENTE".equals(nota.getTipoDePago());
            double saldoPendienteGenerado = 0.0;

            if (notaEnCuentaCorriente) {
                venta.setTotalDeudaCorriente(venta.getTotalDeudaCorriente() + montoNota);
                double saldoAFavorActual = cliente.getSaldoAFavor();
                double saldoAFavorUsado = Math.min(saldoAFavorActual, montoNota);
                saldoPendienteGenerado = montoNota - saldoAFavorUsado;

                if (saldoAFavorUsado > 0) {
                    cliente.setSaldoAFavor(saldoAFavorActual - saldoAFavorUsado);
                    logger.info("Saldo a favor aplicado por nota de débito: clienteId={}, monto={}, saldoAFavor={}",
                            cliente.getId(), saldoAFavorUsado, cliente.getSaldoAFavor());
                }
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
            Long cuentaDebe = switch (nota.getTipoDePago()) {
                case "EFECTIVO" -> CUENTA_CAJA;
                case "DEBITO", "TRANSFERENCIA" -> CUENTA_BANCO;
                default -> CUENTA_DEUDORES_VENTAS;
            };
            movimientoDebe.setCuentaCodigo(cuentaDebe);
            movimientoDebe.setDebe(nota.getMonto());
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
        
            CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
            movimientoHaber.setCuentaCodigo(CUENTA_VENTAS); // El Haber usa la cuenta de Ventas
            movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
            movimientoHaber.setHaber(nota.getMonto());
        
            asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
        
            logger.debug("Generando asiento contable por nota de débito con monto: {}", nota.getMonto());
            asientoServicio.crearAsiento(asientoDTO, usuarioId);
            logger.info("Asiento contable por nota de débito creado exitosamente.");
        }
    
        // Hacemos un saveAndFlush para actualizar el registro con el numeroComprobante correcto y las otras validaciones
        Nota notaGuardada = notaRepositorio.saveAndFlush(nota);
        logger.info("Transacción completada: Nota guardada exitosamente con Comprobante: {} e ID: {}", notaGuardada.getNumeroComprobante(), notaGuardada.getIdNota());
    
        return notaGuardada;
    }
}