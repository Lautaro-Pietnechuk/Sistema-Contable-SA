import React, { useState, useEffect, useCallback } from 'react';
import axios from '../../axiosConfig';
import DetallesClientes from './DetallesClientes';
import EditarCliente from './EditarCliente';

function ListarClientes({ show, handleClose }) {
  const [clientes, setClientes] = useState([]);
  const [filteredClientes, setFilteredClientes] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [clienteToDelete, setClienteToDelete] = useState(null);
  const [clienteToEdit, setClienteToEdit] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [clienteToView, setClienteToView] = useState(null); // Estado para el cliente a ver detalles
  const [showDetailsModal, setShowDetailsModal] = useState(false); // Estado para mostrar el modal de detalles
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [mensajeExito, setMensajeExito] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const fetchClientes = useCallback(async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/clientes');
      const clientesBase = Array.isArray(response.data) ? response.data : [];

      const clientesConDetalle = await Promise.all(
        clientesBase.map(async (cliente) => {
          try {
            const detalle = await axios.get(`http://localhost:8080/api/clientes/${cliente.id}`);
            return { ...cliente, ...detalle.data };
          } catch (errorDetalle) {
            console.error(`No se pudo obtener detalle de cliente ${cliente.id}:`, errorDetalle);
            return cliente;
          }
        })
      );

      setClientes(clientesConDetalle);
      setFilteredClientes(clientesConDetalle);
    } catch (error) {
      console.error('Error al obtener los clientes:', error);
      setErrorMessage('No se pudieron cargar los clientes.');
    }
  }, []);

  useEffect(() => {
    if (show !== false) {
      fetchClientes();
    }
  }, [show, fetchClientes]);

  useEffect(() => {
    if (!show) {
      setSearchTerm('');
      setFilteredClientes(clientes); // Resetear la lista filtrada a la original
    }
  }, [show, clientes]);

  const handleDelete = async () => {
    if (!clienteToDelete?.id) {
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/clientes/${clienteToDelete.id}`);
      setShowDeleteConfirm(false);
      setClienteToDelete(null);
      fetchClientes(); // Refrescar la lista de clientes después de eliminar
      setShowSuccessMessage(true);
      setMensajeExito('Cliente eliminado con éxito');
      setTimeout(() => {
        setShowSuccessMessage(false);
      }, 3000);
    } catch (error) {
      console.error('Error al eliminar el cliente:', error);
      setErrorMessage('Error al eliminar el cliente');
    }
  };

  const confirmDelete = (cliente) => {
    setClienteToDelete(cliente);
    setShowDeleteConfirm(true);
  };

  const handleViewDetails = (cliente) => {
    setClienteToView(cliente);
    setShowDetailsModal(true);
  };

  const handleEdit = (cliente) => {
    setClienteToEdit(cliente);
    setShowEditModal(true);
  };

  const handleClienteUpdated = () => {
    fetchClientes();
  };

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    filterClientes(term);
  };

  const filterClientes = (term) => {
    let filtered = clientes;
    if (term) {
      filtered = filtered.filter((cliente) =>
        (cliente.nombre || '').toLowerCase().includes(term)
        || (cliente.mail || '').toLowerCase().includes(term)
        || (cliente.telefono || '').toLowerCase().includes(term)
      );
    }
    setFilteredClientes(filtered);
  };

  if (show === false) {
    return null;
  }

  return (
    <>
      <div style={{ width: '100%', maxWidth: '1100px', margin: '0 auto', padding: '10px 20px 20px 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>Listar Clientes</h2>
        </div>

        <div style={{ marginTop: '15px' }}>
          <label htmlFor="search"><strong>Buscar por nombre, mail o telefono</strong></label>
          <input
            id="search"
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
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Mail</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Telefono</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(filteredClientes) && filteredClientes.map((cliente) => (
                <tr key={cliente.id}>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{cliente.nombre || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{cliente.mail || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{cliente.telefono || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>
                    <button type="button" onClick={() => handleViewDetails(cliente)}>
                      Detalles
                    </button>
                    <button type="button" onClick={() => handleEdit(cliente)} style={{ marginLeft: '8px' }}>
                      Editar
                    </button>
                    <button type="button" onClick={() => confirmDelete(cliente)} style={{ marginLeft: '8px' }}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {clienteToView && (
        <DetallesClientes
          show={showDetailsModal}
          handleClose={() => setShowDetailsModal(false)}
          cliente={clienteToView}
        />
      )}

      {clienteToEdit && (
        <EditarCliente
          show={showEditModal}
          handleClose={() => setShowEditModal(false)}
          cliente={clienteToEdit}
          onClienteUpdated={handleClienteUpdated}
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

      {showDeleteConfirm && (
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
            <h3 style={{ marginTop: 0 }}>Confirmar Eliminacion</h3>
            <p>
              ¿Estas seguro de que deseas eliminar el cliente "{clienteToDelete?.nombre}"?
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button type="button" onClick={() => setShowDeleteConfirm(false)}>
                Cancelar
              </button>
              <button type="button" onClick={handleDelete}>
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ListarClientes;