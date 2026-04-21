import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

function EditarProducto({ show, handleClose, producto, onProductoUpdated }) {
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [stock, setStock] = useState('');
  const [mensajeError, setMensajeError] = useState('');

  useEffect(() => {
    if (show && producto) {
      setNombre(producto.nombre || '');
      setDescripcion(producto.descripcion || '');
      setPrecio(producto.precio ?? '');
      setStock(producto.stock ?? '');
      setMensajeError('');
    }
  }, [show, producto]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!producto?.id) {
      return;
    }

    try {
      await axios.put(`http://localhost:8080/api/productos/${producto.id}`, {
        nombre,
        descripcion,
        precio: Number(precio),
        stock: Number(stock),
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
          maxWidth: '560px',
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
              <label htmlFor="precio-producto"><strong>Precio</strong></label>
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
              <label htmlFor="stock-producto"><strong>Stock</strong></label>
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
          </div>

          {mensajeError && (
            <p style={{ color: '#842029', marginBottom: '12px' }}>{mensajeError}</p>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
            <button type="button" onClick={handleClose}>Cancelar</button>
            <button type="submit">Guardar Cambios</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditarProducto;