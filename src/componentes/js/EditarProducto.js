import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

function EditarProducto({ show, handleClose, producto, onProductoUpdated }) {
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [stock, setStock] = useState('');
  
  // Nuevo estado para el costo por unidad de la mercadería entrante
  const [costoNuevoUnidad, setCostoNuevoUnidad] = useState(''); 
  const [mensajeError, setMensajeError] = useState('');

  useEffect(() => {
    if (show && producto) {
      setNombre(producto.nombre || '');
      setDescripcion(producto.descripcion || '');
      setPrecio(producto.precio ?? '');
      setStock(producto.stock ?? '');
      setCostoNuevoUnidad(''); // Reiniciamos el input al abrir
      setMensajeError('');
    }
  }, [show, producto]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!producto?.id) {
      return;
    }

    // 1. Calculamos cuántas unidades REALMENTE se están agregando
    const stockIngresado = Number(stock);
    const stockAnterior = Number(producto.stock || 0);
    const stockAgregado = stockIngresado - stockAnterior;

    // 2. Calculamos el costo total de la compra (solo si se agregó stock)
    let costoTotalCompra = 0;
    if (stockAgregado > 0) {
      costoTotalCompra = stockAgregado * Number(costoNuevoUnidad || 0);
    }

    try {
      // 3. Enviamos el DTO en el body y el costoTotalCompra como parámetro URL
      await axios.put(`http://localhost:8080/api/productos/${producto.id}?costoTotalCompra=${costoTotalCompra}`, {
        nombre,
        descripcion,
        precio: Number(precio),
        stock: stockIngresado,
        activo: producto.activo
      });

      if (typeof onProductoUpdated === 'function') {
        onProductoUpdated();
      }

      handleClose();
    } catch (error) {
      console.error('Error al actualizar el producto:', error);
      setMensajeError('No se pudo actualizar el producto.');
    }
  };

  if (!show || !producto) {
    return null;
  }

  // Comprobación para deshabilitar el input de costo si no se está agregando stock
  const estaAgregandoStock = Number(stock) > Number(producto.stock || 0);

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1300
      }}
      onClick={handleClose}
    >
      <div
        style={{
          width: '90%',
          maxWidth: '650px', // Un poco más ancho para que entren las 3 columnas
          backgroundColor: '#fff',
          borderRadius: '10px',
          padding: '20px',
          boxShadow: '0 12px 30px rgba(0, 0, 0, 0.2)'
        }}
        onClick={(event) => event.stopPropagation()}
      >
        <h2 style={{ marginTop: 0 }}>Editar Producto</h2>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '12px' }}>
            <label htmlFor="nombre-producto"><strong>Nombre</strong></label>
            <input
              id="nombre-producto"
              type="text"
              value={nombre}
              onChange={(event) => setNombre(event.target.value)}
              required
              style={{ width: '100%', marginTop: '6px', padding: '8px' }}
            />
          </div>

          <div style={{ marginBottom: '12px' }}>
            <label htmlFor="descripcion-producto"><strong>Descripcion</strong></label>
            <textarea
              id="descripcion-producto"
              value={descripcion}
              onChange={(event) => setDescripcion(event.target.value)}
              rows="3"
              style={{ width: '100%', marginTop: '6px', padding: '8px', resize: 'vertical' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '12px', marginBottom: '12px' }}>
            <div style={{ flex: 1 }}>
              <label htmlFor="precio-producto"><strong>Precio Venta</strong></label>
              <input
                id="precio-producto"
                type="number"
                min="0"
                step="0.01"
                value={precio}
                onChange={(event) => setPrecio(event.target.value)}
                required
                style={{ width: '100%', marginTop: '6px', padding: '8px' }}
              />
            </div>
            <div style={{ flex: 1 }}>
              <label htmlFor="stock-producto"><strong>Stock Total</strong></label>
              <input
                id="stock-producto"
                type="number"
                min="0"
                step="1"
                value={stock}
                onChange={(event) => setStock(event.target.value)}
                required
                style={{ width: '100%', marginTop: '6px', padding: '8px' }}
              />
            </div>
            
            {/* Nueva columna para el costo por unidad */}
            <div style={{ flex: 1 }}>
              <label htmlFor="costo-nuevo-producto">
                <strong>Costo (x Unidad)</strong>
              </label>
              <input
                id="costo-nuevo-producto"
                type="number"
                min="0"
                step="0.01"
                value={costoNuevoUnidad}
                onChange={(event) => setCostoNuevoUnidad(event.target.value)}
                disabled={!estaAgregandoStock}
                required={estaAgregandoStock}
                placeholder={!estaAgregandoStock ? "Solo al sumar stock" : "Ej: 15000.50"}
                style={{ 
                  width: '100%', 
                  marginTop: '6px', 
                  padding: '8px',
                  backgroundColor: !estaAgregandoStock ? '#e9ecef' : '#fff'
                }}
              />
            </div>
          </div>

          {mensajeError && (
            <p style={{ color: '#842029', marginBottom: '12px' }}>{mensajeError}</p>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
            <button type="button" onClick={handleClose} style={{ padding: '8px 16px' }}>Cancelar</button>
            <button type="submit" style={{ padding: '8px 16px', backgroundColor: '#0d6efd', color: '#fff', border: 'none', borderRadius: '4px' }}>Guardar Cambios</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditarProducto;