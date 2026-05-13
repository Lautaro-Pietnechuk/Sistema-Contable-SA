package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.ProductoDTO;
import com.sa.contable.entidades.Producto;
import com.sa.contable.repositorios.ProductoRepositorio;

import jakarta.transaction.Transactional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoServicio {

    @Autowired
    private ProductoRepositorio productoRepositorio;

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

    public ProductoDTO crearProducto(ProductoDTO productoDTO) {

        Producto producto = convertirAEntidad(productoDTO);
        producto.setActivo(true);
        Producto productoGuardado = productoRepositorio.save(producto);
        return convertirADTO(productoGuardado);
    }

    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO, BigDecimal costoTotalCompra     ) {
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