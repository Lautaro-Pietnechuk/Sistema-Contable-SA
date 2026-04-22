import React, { useMemo, useState, useEffect, useCallback } from 'react';
import axios from '../../axiosConfig';
import DetallesVenta from './DetallesVenta';

function ListarVentas({ show, handleClose }) {
  const [ventas, setVentas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [showDetalles, setShowDetalles] = useState(false);
  const [ventaSeleccionada, setVentaSeleccionada] = useState(null);
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [clienteSeleccionado, setClienteSeleccionado] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const fetchVentas = useCallback(async () => {
    try {
      const response = await axios.get('/api/ventas');
      const ventasData = Array.isArray(response.data) ? response.data : [];
      setVentas(ventasData);  
      setErrorMessage('');
    } catch (error) {
      console.error('Error al cargar las ventas:', error);
      const status = error?.response?.status;
      if (status === 401 || status === 403) {
        setErrorMessage('Tu sesion no es valida. Inicia sesion nuevamente.');
      } else {
        setErrorMessage('No se pudieron cargar las ventas.');
      }
    }
  }, []);

  const fetchClientes = useCallback(async () => {
    try {
      const response = await axios.get('/api/clientes');
      setClientes(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error al cargar los clientes:', error);
    }
  }, []);

  useEffect(() => {
    if (show !== false) {
      fetchVentas();
      fetchClientes();
    }
  }, [show, fetchVentas, fetchClientes]);

  const handleShowDetalles = (venta) => {
    setVentaSeleccionada(venta);
    setShowDetalles(true);
  };

  const handleCloseDetalles = () => {
    setShowDetalles(false);
    setVentaSeleccionada(null);
  };

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
  };

  const formatFecha = (fechaIso) => {
    if (!fechaIso) {
      return '-';
    }

    const fecha = new Date(fechaIso);
    if (Number.isNaN(fecha.getTime())) {
      return '-';
    }

    return fecha.toLocaleDateString('es-AR');
  };

  const ventasFiltradas = useMemo(() => {
    return ventas.filter((venta) => {
      const fechaVenta = venta.fecha ? new Date(venta.fecha) : null;
      const inicio = fechaInicio ? new Date(`${fechaInicio}T00:00:00`) : null;
      const fin = fechaFin ? new Date(`${fechaFin}T23:59:59`) : null;
      const cumpleFechaInicio = !inicio || (fechaVenta && fechaVenta >= inicio);
      const cumpleFechaFin = !fin || (fechaVenta && fechaVenta <= fin);

      const cumpleCliente = !clienteSeleccionado || String(venta.clienteId) === String(clienteSeleccionado);

      const termino = searchTerm.trim().toLowerCase();
      const contieneTermino =
        !termino
        || (venta.numeroComprobante || '').toLowerCase().includes(termino)
        || (venta.clienteNombre || '').toLowerCase().includes(termino)
        || (venta.observaciones || '').toLowerCase().includes(termino)
        || (venta.detalles || []).some((detalle) => (detalle.productoNombre || '').toLowerCase().includes(termino));

      return cumpleFechaInicio && cumpleFechaFin && cumpleCliente && contieneTermino;
    });
  }, [ventas, fechaInicio, fechaFin, clienteSeleccionado, searchTerm]);

  if (show === false) {
    return null;
  }

  return (
    <>
      <div style={{ width: '100%', maxWidth: '1180px', margin: '0 auto', padding: '10px 20px 20px 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>Listar Ventas</h2>
          {typeof handleClose === 'function' && (
            <button type="button" onClick={handleClose}>Cerrar</button>
          )}
        </div>

        <div
          style={{
            marginTop: '20px',
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            columnGap: '18px',
            rowGap: '14px',
            alignItems: 'end'
          }}
        >
          <div style={{ minWidth: 0 }}>
            <label htmlFor="fecha-inicio-ventas"><strong>Fecha Inicio</strong></label>
            <input
              id="fecha-inicio-ventas"
              type="date"
              value={fechaInicio}
              onChange={(event) => setFechaInicio(event.target.value)}
              style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}
            />
          </div>

          <div style={{ minWidth: 0 }}>
            <label htmlFor="fecha-fin-ventas"><strong>Fecha Fin</strong></label>
            <input
              id="fecha-fin-ventas"
              type="date"
              value={fechaFin}
              onChange={(event) => setFechaFin(event.target.value)}
              style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}
            />
          </div>

          <div style={{ minWidth: 0 }}>
            <label htmlFor="cliente-ventas"><strong>Cliente</strong></label>
            <select
              id="cliente-ventas"
              value={clienteSeleccionado}
              onChange={(event) => setClienteSeleccionado(event.target.value)}
              style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}
            >
              <option value="">Todos</option>
              {clientes.map((cliente) => (
                <option key={cliente.id} value={cliente.id}>{cliente.nombre}</option>
              ))}
            </select>
          </div>

          <div style={{ gridColumn: '1 / -1' }}>
            <label htmlFor="buscar-ventas"><strong>Buscar (comprobante, cliente, observaciones o producto)</strong></label>
            <input
              id="buscar-ventas"
              type="text"
              placeholder="Buscar..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              style={{ width: '100%', marginTop: '6px', padding: '8px' }}
            />
          </div>
        </div>

        <div style={{ overflowX: 'auto', marginTop: '15px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Comprobante</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Cliente</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Fecha</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Total</th>
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {ventasFiltradas.map((venta) => (
                <tr key={venta.id}>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{venta.numeroComprobante || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{venta.clienteNombre || '-'}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{formatFecha(venta.fecha)}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>{formatMoneda(venta.total)}</td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>
                    <button type="button" onClick={() => handleShowDetalles(venta)}>Detalles</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {ventaSeleccionada && (
        <DetallesVenta
          show={showDetalles}
          handleClose={handleCloseDetalles}
          venta={ventaSeleccionada}
        />
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
    </>
  );
}

export default ListarVentas;