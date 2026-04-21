import React, { useState, useEffect, useCallback } from 'react';
import axios from '../../axiosConfig';
import DetallesProducto from './DetallesProducto';
import EditarProducto from './EditarProducto';

function ListarProductos({ show }) {
  const [productos, setProductos] = useState([]);
  const [filteredProductos, setFilteredProductos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showStatusConfirm, setShowStatusConfirm] = useState(false);
  const [productoToUpdate, setProductoToUpdate] = useState(null);
  const [productoToEdit, setProductoToEdit] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [productoToView, setProductoToView] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [mensajeExito, setMensajeExito] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const fetchProductos = useCallback(async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/productos');
      const productosData = Array.isArray(response.data) ? response.data : [];
      setProductos(productosData);
      setFilteredProductos(productosData);
    } catch (error) {
      console.error('Error al obtener los productos:', error);
      setErrorMessage('No se pudieron cargar los productos.');
    }
  }, []);

  useEffect(() => {
    if (show !== false) {
      fetchProductos();
    }
  }, [show, fetchProductos]);

  useEffect(() => {
    if (!show) {
      setSearchTerm('');
      setFilteredProductos(productos);
    }
  }, [show, productos]);

  const handleStatusChange = async () => {
    if (!productoToUpdate?.id) {
      return;
    }

    try {
      const nuevoEstado = !productoToUpdate.activo;
      await axios.put(`http://localhost:8080/api/productos/${productoToUpdate.id}`, {
        nombre: productoToUpdate.nombre,
        descripcion: productoToUpdate.descripcion,
        precio: Number(productoToUpdate.precio),
        stock: Number(productoToUpdate.stock),
        activo: nuevoEstado
      });

      setShowStatusConfirm(false);
      setProductoToUpdate(null);
      fetchProductos();
      setShowSuccessMessage(true);
      setMensajeExito(nuevoEstado ? 'Producto activado con exito' : 'Producto desactivado con exito');
      setTimeout(() => {
        setShowSuccessMessage(false);
      }, 3000);
    } catch (error) {
      console.error('Error al cambiar estado del producto:', error);
      setErrorMessage('Error al cambiar estado del producto');
    }
  };

  const confirmStatusChange = (producto) => {
    setProductoToUpdate(producto);
    setShowStatusConfirm(true);
  };

  const handleViewDetails = (producto) => {
    setProductoToView(producto);
    setShowDetailsModal(true);
  };

  const handleEdit = (producto) => {
    setProductoToEdit(producto);
    setShowEditModal(true);
  };

  const handleProductoUpdated = () => {
    fetchProductos();
  };

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);

    let filtered = productos;
    if (term) {
      filtered = filtered.filter((producto) =>
        (producto.nombre || '').toLowerCase().includes(term)
        || (producto.descripcion || '').toLowerCase().includes(term)
      );
    }

    setFilteredProductos(filtered);
  };

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
  };

  if (show === false) {
    return null;
  }

  return (
    <>
      <div style={{ width: '100%', maxWidth: '1100px', margin: '0 auto', padding: '10px 20px 20px 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>Listar Productos</h2>
        </div>

        <div style={{ marginTop: '15px' }}>
          <label htmlFor="search-productos"><strong>Buscar por nombre o descripcion</strong></label>
          <input
            id="search-productos"
            type="text"
            placeholder="Buscar..."
            value={searchTerm}
            onChange={handleSearch}
            style={{
              width: '100%',
              marginTop: '8px',
              padding: '8px 10px',
              border: '1px solid #343a40',
              borderRadius: '4px'
            }}
          />
        </div>

        <div style={{ overflowX: 'auto', marginTop: '15px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Nombre</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Descripcion</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Precio</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Stock</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Estado</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(filteredProductos) && filteredProductos.map((producto) => (
                <tr key={producto.id}>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{producto.nombre || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{producto.descripcion || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{formatMoneda(producto.precio)}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{producto.stock ?? 0}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{producto.activo ? 'Activo' : 'Inactivo'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>
                    <button type="button" onClick={() => handleViewDetails(producto)}>
                      Detalles
                    </button>
                    <button type="button" onClick={() => handleEdit(producto)} style={{ marginLeft: '8px' }}>
                      Editar
                    </button>
                    <button
                      type="button"
                      onClick={() => confirmStatusChange(producto)}
                      style={{ marginLeft: '8px' }}
                    >
                      {producto.activo ? 'Desactivar' : 'Activar'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {productoToView && (
        <DetallesProducto
          show={showDetailsModal}
          handleClose={() => setShowDetailsModal(false)}
          producto={productoToView}
        />
      )}

      {productoToEdit && (
        <EditarProducto
          show={showEditModal}
          handleClose={() => setShowEditModal(false)}
          producto={productoToEdit}
          onProductoUpdated={handleProductoUpdated}
        />
      )}

      {showSuccessMessage && (
        <div style={{
          position: 'fixed',
          bottom: '20px',
          right: '20px',
          zIndex: 1200,
          backgroundColor: '#d1e7dd',
          color: '#0f5132',
          padding: '10px 14px',
          borderRadius: '6px',
          border: '1px solid #badbcc'
        }}>
          {mensajeExito}
        </div>
      )}

      {errorMessage && (
        <div style={{
          position: 'fixed',
          bottom: '20px',
          right: '20px',
          zIndex: 1200,
          backgroundColor: '#f8d7da',
          color: '#842029',
          padding: '10px 14px',
          borderRadius: '6px',
          border: '1px solid #f5c2c7'
        }}>
          {errorMessage}
        </div>
      )}

      {showStatusConfirm && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1100
        }}>
          <div style={{ backgroundColor: '#fff', padding: '18px', borderRadius: '8px', width: '90%', maxWidth: '480px' }}>
            <h3 style={{ marginTop: 0 }}>Confirmar Cambio de Estado</h3>
            <p>
              ¿Estas seguro de que deseas {productoToUpdate?.activo ? 'desactivar' : 'activar'} el producto "{productoToUpdate?.nombre}"?
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button type="button" onClick={() => setShowStatusConfirm(false)}>
                Cancelar
              </button>
              <button type="button" onClick={handleStatusChange}>
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ListarProductos;