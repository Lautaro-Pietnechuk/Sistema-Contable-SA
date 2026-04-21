import React, { useEffect, useMemo, useState } from 'react';
import axios from '../../axiosConfig';

const campo = {
	margin: '10px 0',
	padding: '10px 12px',
	backgroundColor: '#f8f9fa',
	borderRadius: '6px',
	border: '1px solid #dee2e6'
};

function DetallesClientes({ show, handleClose, cliente }) {
	const [ventas, setVentas] = useState([]);
	const [loadingVentas, setLoadingVentas] = useState(false);
	const [errorVentas, setErrorVentas] = useState('');
	const [ventasExpandidas, setVentasExpandidas] = useState({});

	useEffect(() => {
		const obtenerVentas = async () => {
			if (!show || !cliente?.id) {
				return;
			}

			setLoadingVentas(true);
			setErrorVentas('');

			try {
				const response = await axios.get(`http://localhost:8080/api/ventas/cliente/${cliente.id}`);
				const ventasCliente = Array.isArray(response.data) ? response.data : [];
				setVentas(ventasCliente);
			} catch (error) {
				console.error('Error al obtener ventas del cliente:', error);
				setErrorVentas('No se pudieron cargar las ventas del cliente.');
			} finally {
				setLoadingVentas(false);
			}
		};

		obtenerVentas();
	}, [show, cliente]);

	const totalAcumulado = useMemo(() => {
		return ventas.reduce((acc, venta) => acc + Number(venta.total || 0), 0);
	}, [ventas]);

	const toggleVentaExpandida = (ventaId) => {
		setVentasExpandidas((prev) => ({
			...prev,
			[ventaId]: !prev[ventaId]
		}));
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

		return fecha.toLocaleString('es-AR');
	};

	if (!show || !cliente) {
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
				zIndex: 1200
			}}
			onClick={handleClose}
		>
			<div
				style={{
					width: '90%',
					maxWidth: '520px',
					backgroundColor: '#fff',
					borderRadius: '10px',
					padding: '20px',
					boxShadow: '0 12px 30px rgba(0, 0, 0, 0.2)'
				}}
				onClick={(e) => e.stopPropagation()}
			>
				<h2 style={{ marginTop: 0 }}>Detalles del Cliente</h2>

				<div style={campo}><strong>ID:</strong> {cliente.id ?? '-'}</div>
				<div style={campo}><strong>Nombre:</strong> {cliente.nombre || '-'}</div>
				<div style={campo}><strong>Mail:</strong> {cliente.mail || '-'}</div>
				<div style={campo}><strong>Telefono:</strong> {cliente.telefono || '-'}</div>

				<h3 style={{ marginTop: '20px', marginBottom: '8px' }}>Ventas del cliente</h3>
				<div style={campo}><strong>Total vendido acumulado:</strong> {formatMoneda(totalAcumulado)}</div>

				{loadingVentas && <p style={{ margin: '8px 0' }}>Cargando ventas...</p>}
				{errorVentas && <p style={{ margin: '8px 0', color: '#842029' }}>{errorVentas}</p>}

				{!loadingVentas && !errorVentas && ventas.length === 0 && (
					<p style={{ margin: '8px 0' }}>Este cliente no tiene ventas registradas.</p>
				)}

				{!loadingVentas && !errorVentas && ventas.length > 0 && (
					<div style={{ marginTop: '10px', maxHeight: '260px', overflowY: 'auto', border: '1px solid #dee2e6', borderRadius: '6px' }}>
						{ventas.map((venta) => {
							const expandida = !!ventasExpandidas[venta.id];
							const detalles = Array.isArray(venta.detalles) ? venta.detalles : [];

							return (
								<div key={venta.id} style={{ borderBottom: '1px solid #dee2e6', padding: '10px' }}>
									<div style={{ display: 'flex', justifyContent: 'space-between', gap: '10px', alignItems: 'center' }}>
										<div>
											<div><strong>Comprobante:</strong> {venta.numeroComprobante || '-'}</div>
											<div><strong>Fecha:</strong> {formatFecha(venta.fecha)}</div>
											<div><strong>Total:</strong> {formatMoneda(venta.total)}</div>
										</div>
										<button type="button" onClick={() => toggleVentaExpandida(venta.id)}>
											{expandida ? 'Ocultar items' : 'Ver items'}
										</button>
									</div>

									{expandida && (
										<div style={{ marginTop: '8px', padding: '8px', backgroundColor: '#f8f9fa', borderRadius: '6px' }}>
											{detalles.length === 0 ? (
												<p style={{ margin: 0 }}>Sin items cargados.</p>
											) : (
												detalles.map((detalle) => (
													<div key={detalle.id || `${venta.id}-${detalle.productoId}`} style={{ padding: '6px 0', borderBottom: '1px dashed #ced4da' }}>
														<div><strong>Producto:</strong> {detalle.productoNombre || '-'}</div>
														<div><strong>Cantidad:</strong> {detalle.cantidad ?? 0}</div>
														<div><strong>Precio unitario:</strong> {formatMoneda(detalle.precioUnitario)}</div>
														<div><strong>Subtotal:</strong> {formatMoneda(detalle.subtotal)}</div>
													</div>
												))
											)}
										</div>
									)}
								</div>
							);
						})}
					</div>
				)}

				<div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '15px' }}>
					<button type="button" onClick={handleClose}>Cerrar</button>
				</div>
			</div>
		</div>
	);
}

export default DetallesClientes;
