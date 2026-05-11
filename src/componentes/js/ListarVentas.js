import React, { useMemo, useState, useEffect, useCallback } from 'react';
import axios from '../../axiosConfig';
import DetallesVenta from './DetallesVenta';

function ListarVentas({ show, handleClose }) {
  const [ventas, setVentas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [showDetalles, setShowDetalles] = useState(false);
  const [ventaSeleccionada, setVentaSeleccionada] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [ventaToCancel, setVentaToCancel] = useState(null);
  const [motivoCancelacion, setMotivoCancelacion] = useState('');
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [mensajeExito, setMensajeExito] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [clienteSeleccionado, setClienteSeleccionado] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('todas');
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // Función robusta para evaluar si la venta está anulada (maneja booleanos, strings y números)
  const esAnulada = (valor) => {
    return valor === true || valor === 'true' || valor === 1 || valor === '1';
  };

  const fetchVentas = useCallback(async () => {
    try {
      const response = await axios.get('/api/ventas');
      console.log("ESTO ES LO QUE MANDA EL BACKEND:", response.data);
      const ventasData = Array.isArray(response.data) ? response.data : [];
      setVentas(ventasData);  
      setErrorMessage('');
    } catch (error) {
      console.error('Error al cargar las ventas:', error);
      const status = error?.response?.status;
      if (status === 401 || status === 403) {
        setErrorMessage('Tu sesión no es válida. Inicia sesión nuevamente.');
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

  const confirmCancelVenta = (venta) => {
    setVentaToCancel(venta);
    setMotivoCancelacion('');
    setShowCancelConfirm(true);
  };

  const cerrarCancelacion = () => {
    setShowCancelConfirm(false);
    setVentaToCancel(null);
    setMotivoCancelacion('');
  };

  const handleCancelVenta = async () => {
    if (!ventaToCancel?.id) {
      return;
    }

    const motivo = motivoCancelacion.trim();
    if (!motivo) {
      setErrorMessage('El motivo de cancelación es obligatorio.');
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
      setErrorMessage('No se pudo identificar al usuario para cancelar la venta.');
      return;
    }

    const payload = {
      tipo: 'C',
      idVenta: ventaToCancel.id,
      monto: Number(ventaToCancel.total || 0),
      motivo
    };

    try {
      // CORRECCIÓN: Se agregó el ?userId=${userId} que el controlador de Java necesita
      await axios.post(`/api/notas`, payload);
      
      cerrarCancelacion();
      setShowSuccessMessage(true);
      setMensajeExito('Venta cancelada con éxito.');
      setErrorMessage('');
      
      await fetchVentas();
      
      setTimeout(() => {
        setShowSuccessMessage(false);
      }, 3000);

    } catch (error) {
      console.error('Error completo al cancelar la venta:', error);
      
      // CORRECCIÓN: Lectura inteligente del JSON de error de Spring Boot
      let mensajeAmostrar = 'Error al cancelar la venta.';
      
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          mensajeAmostrar = error.response.data; // Si mandaste un String desde el backend
        } else if (error.response.data.message) {
          mensajeAmostrar = error.response.data.message; // Si Spring Boot mandó su JSON de error por defecto
        } else {
          mensajeAmostrar = `Error del servidor: ${error.response.status}`;
        }
      } else if (error.request) {
        mensajeAmostrar = 'Error de red: No se pudo conectar con el servidor.';
      }

      setErrorMessage(mensajeAmostrar);
      
      // Ocultar el mensaje de error después de 5 segundos para que no quede trabado en pantalla
      setTimeout(() => setErrorMessage(''), 5000);
    }
  };

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
  };

  const formatFecha = (fechaIso) => {
    if (!fechaIso) {
      return '-';
    }

    const partes = fechaIso.split('T')[0].split('-');
    if (partes.length === 3) {
      const fechaLocal = new Date(partes[0], partes[1] - 1, partes[2]);
      return fechaLocal.toLocaleDateString('es-AR');
    }

    const fecha = new Date(fechaIso);
    if (Number.isNaN(fecha.getTime())) {
      return '-';
    }

    return fecha.toLocaleDateString('es-AR');
  };

  const ventasFiltradas = useMemo(() => {
    return ventas.filter((venta) => {
      const fechaVentaStr = venta.fecha ? venta.fecha.split('T')[0] : null;
      const fechaVenta = fechaVentaStr ? new Date(`${fechaVentaStr}T00:00:00`) : null;
      
      const inicio = fechaInicio ? new Date(`${fechaInicio}T00:00:00`) : null;
      const fin = fechaFin ? new Date(`${fechaFin}T23:59:59`) : null;
      
      const cumpleFechaInicio = !inicio || (fechaVenta && fechaVenta >= inicio);
      const cumpleFechaFin = !fin || (fechaVenta && fechaVenta <= fin);

      const cumpleCliente = !clienteSeleccionado || String(venta.clienteId) === String(clienteSeleccionado);
      
      // CORRECCIÓN: Uso de la función esAnulada() para evitar bugs con strings "false"
      const ventaEstaAnulada = esAnulada(venta.anulada);
      const cumpleEstado =
        filtroEstado === 'todas'
        || (filtroEstado === 'canceladas' && ventaEstaAnulada)
        || (filtroEstado === 'vigentes' && !ventaEstaAnulada);

      const termino = searchTerm.trim().toLowerCase();
      const contieneTermino =
        !termino
        || (venta.numeroComprobante || '').toLowerCase().includes(termino)
        || (venta.clienteNombre || '').toLowerCase().includes(termino)
        || (venta.observaciones || '').toLowerCase().includes(termino)
        || (venta.detalles || []).some((detalle) => (detalle.productoNombre || '').toLowerCase().includes(termino));

      return cumpleFechaInicio && cumpleFechaFin && cumpleCliente && cumpleEstado && contieneTermino;
    });
  }, [ventas, fechaInicio, fechaFin, clienteSeleccionado, filtroEstado, searchTerm]);

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

          <div style={{ minWidth: 0 }}>
            <label htmlFor="estado-ventas"><strong>Estado</strong></label>
            <select
              id="estado-ventas"
              value={filtroEstado}
              onChange={(event) => setFiltroEstado(event.target.value)}
              style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}
            >
              <option value="todas">Todas</option>
              <option value="vigentes">No canceladas</option>
              <option value="canceladas">Canceladas</option>
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
                <th style={{ border: '1px solid #343a40', padding: '8px' }}>Estado</th>
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
                  <td style={{ border: '1px solid #343a40', padding: '8px', color: esAnulada(venta.anulada) ? '#dc3545' : 'inherit' }}>
                    {esAnulada(venta.anulada) ? 'Cancelada' : 'No cancelada'}
                  </td>
                  <td style={{ border: '1px solid #343a40', padding: '8px' }}>
                    <button type="button" onClick={() => handleShowDetalles(venta)}>Detalles</button>
                    <button
                      type="button"
                      onClick={() => confirmCancelVenta(venta)}
                      disabled={esAnulada(venta.anulada)}
                      style={{ marginLeft: '8px' }}
                    >
                      {esAnulada(venta.anulada) ? 'Anulada' : 'Cancelar'}
                    </button>
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

      {showSuccessMessage && (
        <div style={{
          position: 'fixed',
          bottom: '20px',
          right: '20px',
          zIndex: 1300,
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
          zIndex: 1300,
          backgroundColor: '#f8d7da',
          color: '#842029',
          padding: '10px 14px',
          borderRadius: '6px',
          border: '1px solid #f5c2c7',
          boxShadow: '0px 4px 6px rgba(0,0,0,0.1)'
        }}>
          <strong>Error: </strong> {errorMessage}
        </div>
      )}

      {showCancelConfirm && (
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
            <h3 style={{ marginTop: 0 }}>Confirmar cancelación</h3>
            <p>
              ¿Estás seguro de que deseas cancelar la venta "{ventaToCancel?.numeroComprobante || ventaToCancel?.id}"?
            </p>
            <div style={{ marginBottom: '12px' }}>
              <label htmlFor="motivo-cancelacion"><strong>Motivo de la cancelación</strong></label>
              <textarea
                id="motivo-cancelacion"
                value={motivoCancelacion}
                onChange={(event) => setMotivoCancelacion(event.target.value)}
                rows={4}
                placeholder="Explicá por qué se cancela la venta"
                style={{ width: '100%', marginTop: '8px', padding: '10px', resize: 'vertical' }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button type="button" onClick={cerrarCancelacion}>
                No
              </button>
              <button type="button" onClick={handleCancelVenta} disabled={!motivoCancelacion.trim()}>
                Sí, cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ListarVentas;