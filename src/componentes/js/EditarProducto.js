import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

function EditarProducto({ show, handleClose, producto, onProductoUpdated }) {
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [stock, setStock] = useState('');
  
  // Estados para el ingreso de nueva mercadería
  const [costoNuevoUnidad, setCostoNuevoUnidad] = useState(''); 
  const [metodoPago, setMetodoPago] = useState('EFECTIVO'); // Nuevo estado para el pago
  
  const [mensajeError, setMensajeError] = useState('');

  useEffect(() => {
    if (show && producto) {
      setNombre(producto.nombre || '');
      setDescripcion(producto.descripcion || '');
      setPrecio(producto.precio ?? '');
      setStock(producto.stock ?? '');
      setCostoNuevoUnidad(''); 
      setMetodoPago('EFECTIVO'); // Reiniciamos al valor por defecto
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
      // 3. Enviamos el DTO en el body (incluyendo metodoPago) y el costoTotalCompra como parámetro URL
      await axios.put(`http://localhost:8080/api/productos/${producto.id}?costoTotalCompra=${costoTotalCompra}`, {
        nombre,
        descripcion,
        precio: Number(precio),
        stock: stockIngresado,
        activo: producto.activo,
        metodoPago: estaAgregandoStock ? metodoPago : null // Mandamos el método de pago solo si hubo compra
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

  // Comprobación para deshabilitar los inputs de compra si no se está agregando stock
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
          maxWidth: '600px', 
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
              style={{ width: '100%', marginTop: '6px', padding: '8px', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ marginBottom: '12px' }}>
            <label htmlFor="descripcion-producto"><strong>Descripcion</strong></label>
            <textarea
              id="descripcion-producto"
              value={descripcion}
              onChange={(event) => setDescripcion(event.target.value)}
              rows="2"
              style={{ width: '100%', marginTop: '6px', padding: '8px', resize: 'vertical', boxSizing: 'border-box' }}
            />
          </div>

          {/* Fila 1: Datos de Venta y Stock Total */}
          <div style={{ display: 'flex', gap: '12px', marginBottom: '15px' }}>
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
                style={{ width: '100%', marginTop: '6px', padding: '8px', boxSizing: 'border-box' }}
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
                style={{ width: '100%', marginTop: '6px', padding: '8px', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          {/* Fila 2: Datos de Ingreso de Mercadería (Con fondo resaltado) */}
          <div style={{ 
            display: 'flex', 
            gap: '12px', 
            marginBottom: '15px', 
            padding: '12px', 
            backgroundColor: estaAgregandoStock ? '#e8f4fd' : '#f8f9fa', 
            borderRadius: '6px',
            border: '1px solid #dee2e6'
          }}>
            <div style={{ flex: 1 }}>
              <label htmlFor="costo-nuevo-producto" style={{ color: !estaAgregandoStock ? '#6c757d' : '#000' }}>
                <strong>Costo (x Unidad Nueva)</strong>
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
                  backgroundColor: !estaAgregandoStock ? '#e9ecef' : '#fff',
                  boxSizing: 'border-box',
                  border: '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
            </div>

            <div style={{ flex: 1 }}>
              <label htmlFor="metodo-pago-producto" style={{ color: !estaAgregandoStock ? '#6c757d' : '#000' }}>
                <strong>Método de Pago</strong>
              </label>
              <select
                id="metodo-pago-producto"
                value={metodoPago}
                onChange={(event) => setMetodoPago(event.target.value)}
                disabled={!estaAgregandoStock}
                style={{ 
                  width: '100%', 
                  marginTop: '6px', 
                  padding: '8px',
                  backgroundColor: !estaAgregandoStock ? '#e9ecef' : '#fff',
                  boxSizing: 'border-box',
                  border: '1px solid #ccc',
                  borderRadius: '4px'
                }}
              >
                <option value="EFECTIVO">Efectivo</option>
                <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                <option value="CREDITO">Credito</option>
              </select>
            </div>
          </div>

          {mensajeError && (
            <p style={{ color: '#842029', marginBottom: '12px', fontWeight: 'bold', textAlign: 'center' }}>{mensajeError}</p>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
            <button type="button" onClick={handleClose} style={{ padding: '10px 16px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
              Cancelar
            </button>
            <button type="submit" style={{ padding: '10px 16px', backgroundColor: '#0d6efd', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
              Guardar Cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditarProducto;