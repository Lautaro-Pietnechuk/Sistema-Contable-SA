import React, { useMemo, useState, useEffect, useCallback } from 'react';
import axios from '../../axiosConfig';
import DetallesVenta from './DetallesVenta';
import NotaDebito from './NotaDebito'; 
import { jsPDF } from 'jspdf'; 
import autoTable from 'jspdf-autotable'; 

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

  const [showNotaDebito, setShowNotaDebito] = useState(false);
  const [ventaParaNotaDebito, setVentaParaNotaDebito] = useState(null);

  const esAnulada = (estado) => {
    return estado === "ANULADA";
  };

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

  const handleShowNotaDebito = (venta) => {
    setVentaParaNotaDebito(venta);
    setShowNotaDebito(true);
  };

  const handleCloseNotaDebito = () => {
    setShowNotaDebito(false);
    setVentaParaNotaDebito(null);
    fetchVentas(); 
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
    if (!ventaToCancel?.id) return;

    const motivo = motivoCancelacion.trim();
    if (!motivo) {
      setErrorMessage('El motivo de cancelación es obligatorio.');
      return;
    }

    const payload = {
      tipo: 'C',
      idVenta: ventaToCancel.id,
      monto: Number(ventaToCancel.total || 0),
      motivo
    };

    try {
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
      console.error('Error al cancelar la venta:', error);
      let mensajeAmostrar = 'Error al cancelar la venta.';
      if (error.response?.data) {
        mensajeAmostrar = typeof error.response.data === 'string' ? error.response.data : error.response.data.message || mensajeAmostrar;
      }
      setErrorMessage(mensajeAmostrar);
    }
  };

  const generarPDFVenta = (ventaData) => {
    try {
      const doc = new jsPDF();
      doc.setFont('helvetica', 'bold');
      doc.text('FACTURA DE VENTA', 14, 20);

      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      doc.text(`Comprobante: ${ventaData.numeroComprobante || '-'}`, 14, 30);
      doc.text(`Fecha: ${formatFecha(ventaData.fecha)}`, 14, 37);
      doc.text(`Cliente: ${ventaData.cliente?.nombre || ventaData.clienteNombre || '-'}`, 14, 44);
      doc.text(`Método de Pago: ${ventaData.tipoDePago || 'EFECTIVO'}`, 14, 51); 

      const detalles = (ventaData.detalles || []).map(d => [
        d.productoNombre || 'Producto',
        d.cantidad || 0,
        `$${Number(d.precioUnitario || d.subtotal / d.cantidad || 0).toFixed(2)}`,
        `$${Number(d.subtotal || 0).toFixed(2)}`
      ]);

      autoTable(doc, {
        head: [['Producto', 'Cantidad', 'Precio Unit.', 'Subtotal']],
        body: detalles,
        startY: 58,
        margin: { left: 14, right: 14 },
        styles: { fontSize: 10 },
        headStyles: { fillColor: [41, 128, 185] }
      });

      const finalY = doc.lastAutoTable.finalY || 100;
      doc.setFont('helvetica', 'bold');
      doc.text(`Total: $${Number(ventaData.total || 0).toFixed(2)}`, 14, finalY + 10);

      if (ventaData.observaciones) {
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(9);
        doc.text(`Observaciones: ${ventaData.observaciones}`, 14, finalY + 20);
      }

      doc.save(`Factura_${ventaData.numeroComprobante || ventaData.id}.pdf`);
    } catch (error) {
      console.error('Error al generar el PDF:', error);
    }
  };

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
  };

  const formatFecha = (fechaIso) => {
    if (!fechaIso) return '-';
    const partes = fechaIso.split('T')[0].split('-');
    if (partes.length === 3) {
      const fechaLocal = new Date(partes[0], partes[1] - 1, partes[2]);
      return fechaLocal.toLocaleDateString('es-AR');
    }
    const fecha = new Date(fechaIso);
    if (Number.isNaN(fecha.getTime())) return '-';
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
      const cumpleCliente = !clienteSeleccionado || String(venta.cliente?.id || venta.clienteId) === String(clienteSeleccionado);
      
      const ventaEstaAnulada = esAnulada(venta.estado);
      const cumpleEstado =
        filtroEstado === 'todas'
        || (filtroEstado === 'canceladas' && ventaEstaAnulada)
        || (filtroEstado === 'vigentes' && !ventaEstaAnulada);

      const termino = searchTerm.trim().toLowerCase();
      const contieneTermino =
        !termino
        || (venta.numeroComprobante || '').toLowerCase().includes(termino)
        || (venta.cliente?.nombre || venta.clienteNombre || '').toLowerCase().includes(termino)
        || (venta.observaciones || '').toLowerCase().includes(termino)
        || (venta.detalles || []).some((detalle) => (detalle.productoNombre || '').toLowerCase().includes(termino));

      return cumpleFechaInicio && cumpleFechaFin && cumpleCliente && cumpleEstado && contieneTermino;
    });
  }, [ventas, fechaInicio, fechaFin, clienteSeleccionado, filtroEstado, searchTerm]);

  if (show === false) return null;

  const obtenerColorEstado = (estado) => {
    if (estado === "ANULADA") return '#dc3545'; 
    if (estado === "PAGADA") return '#198754';  
    return '#ffc107'; 
  };

  // FORZAMOS EL ESTILO AZUL INSTITUCIONAL PARA BOTONES Y CABECERA CON !IMPORTANT
  const forcedBlueThemeCss = `
    .azul-header-row {
      background-color: #4a90e2 !important;
      color: white !important;
    }
    .azul-header-row th {
      color: white !important;
      font-weight: bold !important;
      border: 1px solid #ddd !important;
      padding: 12px !important;
    }
    .sistema-blue-btn {
      background-color: #007bff !important;
      color: white !important;
      border: 1px solid #007bff !important;
      font-size: 14px !important;
      padding: 6px 14px !important;
      border-radius: 4px !important;
      cursor: pointer !important;
      font-weight: bold !important;
      transition: all 0.2s ease !important;
    }
    .sistema-blue-btn:hover {
      background-color: #0056b3 !important;
      border-color: #004085 !important;
    }
    .sistema-blue-btn:disabled {
      background-color: #e9ecef !important;
      color: #6c757d !important;
      border-color: #ced4da !important;
      cursor: not-allowed !important;
      opacity: 0.65 !important;
    }
  `;

  return (
    <>
      <style>{forcedBlueThemeCss}</style>

      <div style={{ width: '100%', maxWidth: '1180px', margin: '0 auto', padding: '10px 20px 20px 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>Listar Ventas</h2>
          {typeof handleClose === 'function' && (
            <button type="button" onClick={handleClose}>Cerrar</button>
          )}
        </div>

        {/* Bloque de Filtros */}
        <div style={{ marginTop: '20px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', columnGap: '18px', rowGap: '14px', alignItems: 'end' }}>
          <div>
            <label htmlFor="fecha-inicio-ventas"><strong>Fecha Inicio</strong></label>
            <input id="fecha-inicio-ventas" type="date" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }} />
          </div>

          <div>
            <label htmlFor="fecha-fin-ventas"><strong>Fecha Fin</strong></label>
            <input id="fecha-fin-ventas" type="date" value={fechaFin} onChange={(e) => setFechaFin(e.target.value)} style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }} />
          </div>

          <div>
            <label htmlFor="cliente-ventas"><strong>Cliente</strong></label>
            <select id="cliente-ventas" value={clienteSeleccionado} onChange={(e) => setClienteSeleccionado(e.target.value)} style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}>
              <option value="">Todos</option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="estado-ventas"><strong>Estado</strong></label>
            <select id="estado-ventas" value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)} style={{ width: '100%', marginTop: '8px', padding: '9px 10px' }}>
              <option value="todas">Todas</option>
              <option value="vigentes">Vigentes (No canceladas)</option>
              <option value="canceladas">Canceladas</option>
            </select>
          </div>

          <div style={{ gridColumn: '1 / -1' }}>
            <label htmlFor="buscar-ventas"><strong>Buscar (comprobante, cliente, observaciones o producto)</strong></label>
            <input id="buscar-ventas" type="text" placeholder="Buscar..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} style={{ width: '100%', marginTop: '6px', padding: '8px' }} />
          </div>
        </div>

        {/* Tabla principal */}
        <div style={{ overflowX: 'auto', marginTop: '25px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              {/* CORREGIDO: Fila de cabecera azul obligatoria con texto blanco legible */}
              <tr className="azul-header-row">
                <th>Comprobante</th>
                <th>Cliente</th>
                <th>Fecha</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {ventasFiltradas.map((venta) => (
                <tr key={venta.id} style={{ borderBottom: '1px solid #ddd' }}>
                  <td style={{ padding: '12px' }}>{venta.numeroComprobante || '-'}</td>
                  <td style={{ padding: '12px' }}>{venta.cliente?.nombre || venta.clienteNombre || '-'}</td>
                  <td style={{ padding: '12px' }}>{formatFecha(venta.fecha)}</td>
                  <td style={{ padding: '12px' }}>{formatMoneda(venta.total)}</td>
                  
                  <td style={{ padding: '12px', color: obtenerColorEstado(venta.estado), fontWeight: 'bold' }}>
                    {venta.estado === 'ANULADA' ? 'Cancelada' : venta.estado === 'PAGADA' ? 'Pagada' : 'Pendiente'}
                  </td>

                  {/* RESTAURADO: Botones azules clásicos del sistema */}
                  <td style={{ padding: '8px' }}>
                    <button 
                      type="button" 
                      onClick={() => handleShowDetalles(venta)}
                      className="sistema-blue-btn"
                    >
                      Detalles
                    </button>
                    
                    <button 
                      type="button" 
                      onClick={() => generarPDFVenta(venta)} 
                      className="sistema-blue-btn"
                      style={{ marginLeft: '6px' }}
                    >
                      Comprobante
                    </button>
                    
                    <button 
                      type="button" 
                      onClick={() => handleShowNotaDebito(venta)} 
                      disabled={esAnulada(venta.estado)}
                      className="sistema-blue-btn"
                      style={{ marginLeft: '6px' }}
                    >
                      Nota Débito
                    </button>

                    <button
                      type="button"
                      onClick={() => confirmCancelVenta(venta)}
                      disabled={esAnulada(venta.estado)}
                      className="sistema-blue-btn"
                      style={{ marginLeft: '6px' }}
                    >
                      {esAnulada(venta.estado) ? 'Anulada' : 'Cancelar'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal de Detalles de Venta */}
      {ventaSeleccionada && (
        <DetallesVenta show={showDetalles} handleClose={handleCloseDetalles} venta={ventaSeleccionada} />
      )}

      {/* Modal de la Nota de Débito */}
      {ventaParaNotaDebito && (
        <NotaDebito show={showNotaDebito} handleClose={handleCloseNotaDebito} ventaSeleccionada={ventaParaNotaDebito} />
      )}

      {/* Alertas Flotantes */}
      {showSuccessMessage && (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1300, backgroundColor: '#d1e7dd', color: '#0f5132', padding: '10px 14px', borderRadius: '6px', border: '1px solid #badbcc' }}>
          {mensajeExito}
        </div>
      )}

      {errorMessage && (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1300, backgroundColor: '#f8d7da', color: '#842029', padding: '10px 14px', borderRadius: '6px', border: '1px solid #f5c2c7', boxShadow: '0px 4px 6px rgba(0,0,0,0.1)' }}>
          <strong>Error: </strong> {errorMessage}
        </div>
      )}

      {/* Modal de confirmación para anulación */}
      {showCancelConfirm && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1100 }}>
          <div style={{ backgroundColor: '#fff', padding: '18px', borderRadius: '8px', width: '90%', maxWidth: '480px' }}>
            <h3 style={{ marginTop: 0 }}>Confirmar cancelación</h3>
            <p>¿Estás seguro de que deseas cancelar la venta "{ventaToCancel?.numeroComprobante || ventaToCancel?.id}"?</p>
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
              <button type="button" onClick={cerrarCancelacion}>No</button>
              <button type="button" onClick={handleCancelVenta} disabled={!motivoCancelacion.trim()}>Sí, cancelar</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ListarVentas;