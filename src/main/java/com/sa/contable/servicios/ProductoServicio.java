package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.dto.ProductoDTO;
import com.sa.contable.entidades.Producto;
import com.sa.contable.repositorios.ProductoRepositorio;

import jakarta.transaction.Transactional;

@Service
public class ProductoServicio {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private AsientoServicio asientoServicio;

    private static final Long cuentaDebe = 131L; // Mercaderías
    private static final Long cuentaHaberCredito = 211L; //  Proveedores - Crédito
    private static final Long cuentaHaberEfectivo = 111L; //  Caja - Efectivo
    private static final Long cuentaHaberDebito = 113L; //  Banco c/c - Débito


    private static final Logger logger = LoggerFactory.getLogger(ProductoServicio.class);

    public List<ProductoDTO> obtenerTodos() {
        return productoRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<ProductoDTO> obtenerActivos() {
        return productoRepositorio.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        return convertirADTO(producto);
    }


    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepositorio.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO, Long usuarioId, Long id, BigDecimal costoTotalCompra) {
        Producto producto = convertirAEntidad(productoDTO);
        producto.setActivo(true);


 // Crear Asiento Contable
        AsientoDTO asientoDTO = new AsientoDTO();
        asientoDTO.setFecha(java.time.LocalDate.now());
        asientoDTO.setDescripcion("Compra de productos: " + producto.getNombre());
        asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
            
        CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
        movimientoDebe.setCuentaCodigo(cuentaDebe);
        movimientoDebe.setDebe(costoTotalCompra);
        movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
            
        CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
        switch (productoDTO.getTipoDePago()) {
            case "EFECTIVO":
                movimientoHaber.setCuentaCodigo(cuentaHaberEfectivo);
                break;
            case "DEBITO":
                movimientoHaber.setCuentaCodigo(cuentaHaberDebito);
                break;
            case "CREDITO":
                movimientoHaber.setCuentaCodigo(cuentaHaberCredito);
                break;
            default:
                throw new RuntimeException("Tipo de pago no válido. Debe ser: EFECTIVO, DEBITO o CREDITO");
        }
        movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
        movimientoHaber.setHaber(costoTotalCompra);

        asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
        asientoServicio.crearAsiento(asientoDTO, usuarioId);

        
        Producto productoGuardado = productoRepositorio.save(producto);
        return convertirADTO(productoGuardado);
    }

    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, BigDecimal costoTotalCompra, Long usuarioId) {
        Producto productoExistente = productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        int cantidadComprada = productoDTO.getStock() - productoExistente.getStock();

        if (cantidadComprada > 0 && productoExistente.getCostoPromedio() != productoDTO.getCostoPromedio()) {
            Producto productoActualizado = ingresarMercaderia(id, cantidadComprada, costoTotalCompra);
            productoExistente.setCostoPromedio(productoActualizado.getCostoPromedio());
            productoExistente.setStock(productoActualizado.getStock());
        } else {
            productoExistente.setStock(productoDTO.getStock());
        }

        if (productoDTO.getActivo() != null) {
            productoExistente.setActivo(productoDTO.getActivo());
        }

        AsientoDTO asientoDTO = new AsientoDTO();
        asientoDTO.setFecha(java.time.LocalDate.now());
        asientoDTO.setDescripcion("Compra de productos: " + productoExistente.getNombre());
        asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
            
        CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
        movimientoDebe.setCuentaCodigo(cuentaDebe);
        movimientoDebe.setDebe(costoTotalCompra);
        movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
            
        CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
        switch (productoDTO.getTipoDePago()) {
            case "EFECTIVO":
                movimientoHaber.setCuentaCodigo(cuentaHaberEfectivo);
                break;
            case "DEBITO":
                movimientoHaber.setCuentaCodigo(cuentaHaberDebito);
                break;
            case "CREDITO":
                movimientoHaber.setCuentaCodigo(cuentaHaberCredito);
                break;
            default:
                throw new RuntimeException("Tipo de pago no válido. Debe ser: EFECTIVO, DEBITO o CREDITO");
        }
        movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
        movimientoHaber.setHaber(costoTotalCompra);

        asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
        asientoServicio.crearAsiento(asientoDTO, usuarioId);

        Producto productoActualizado = productoRepositorio.save(productoExistente);
        return convertirADTO(productoActualizado);
    }

    public void eliminarProducto(Long id) {
        Producto producto = productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setActivo(false);
        productoRepositorio.save(producto);
    }

    public void eliminarProductoDefinitivo(Long id) {
        if (!productoRepositorio.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoRepositorio.deleteById(id);
    }

    // Métodos de conversión

    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setActivo(producto.getActivo());
        dto.setCostoPromedio(producto.getCostoPromedio());
        return dto;
    }

    private Producto convertirAEntidad(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCostoPromedio(dto.getCostoPromedio());
        return producto;
    }


    @Transactional
    public Producto ingresarMercaderia(Long idProducto, int cantidadComprada, BigDecimal costoTotalCompra) {
        Producto producto = productoRepositorio.findById(idProducto)
        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        logger.info("Ingresando mercadería para el producto ID: {}. Cantidad comprada: {}, Costo total de compra: {}", idProducto, cantidadComprada, costoTotalCompra);
    // 1. Plata que ya tenías en stock: (Stock actual * Costo Promedio actual)
        BigDecimal valorStockViejo = producto.getCostoPromedio().multiply(new BigDecimal(producto.getStock()));

    // 2. Sumamos toda la plata (Lo viejo + Lo nuevo)
        BigDecimal plataTotalEnGalpon = valorStockViejo.add(costoTotalCompra);

    // 3. Sumamos el stock físico
        int nuevoStockTotal = producto.getStock() + cantidadComprada;

    // 4. Calculamos el nuevo PPP (Plata Total / Unidades Totales)
        BigDecimal nuevoPPP = plataTotalEnGalpon.divide(new BigDecimal(nuevoStockTotal), 2, RoundingMode.HALF_UP);

    // 5. Guardamos
        producto.setStock(nuevoStockTotal);
        producto.setCostoPromedio(nuevoPPP);
        productoRepositorio.save(producto);
        return producto;
    }
}