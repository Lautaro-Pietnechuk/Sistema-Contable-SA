import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

function EditarCliente({ show, handleClose, cliente, onClienteUpdated }) {
	const [nombre, setNombre] = useState('');
	const [mail, setMail] = useState('');
	const [telefono, setTelefono] = useState('');
	const [mensajeError, setMensajeError] = useState('');

	useEffect(() => {
		if (show && cliente) {
			setNombre(cliente.nombre || '');
			setMail(cliente.mail || '');
			setTelefono(cliente.telefono || '');
			setMensajeError('');
		}
	}, [show, cliente]);

	const handleSubmit = async (e) => {
		e.preventDefault();
		if (!cliente?.id) {
			return;
		}

		try {
			await axios.put(`http://localhost:8080/api/clientes/${cliente.id}`, {
				nombre,
				mail: mail || null,
				telefono: telefono || null
			});

			if (typeof onClienteUpdated === 'function') {
				onClienteUpdated();
			}

			handleClose();
		} catch (error) {
			console.error('Error al actualizar el cliente:', error);
			setMensajeError('No se pudo actualizar el cliente.');
		}
	};

	const handleTelefonoChange = (e) => {
		const value = e.target.value;
		if (/^\d*$/.test(value)) {
			setTelefono(value);
		}
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
				<h2 style={{ marginTop: 0 }}>Editar Cliente</h2>

				<form onSubmit={handleSubmit}>
					<div style={{ marginBottom: '12px' }}>
						<label htmlFor="nombre-cliente"><strong>Nombre</strong></label>
						<input
							id="nombre-cliente"
							type="text"
							value={nombre}
							onChange={(event) => setNombre(event.target.value)}
							required
							style={{ width: '100%', marginTop: '6px', padding: '8px' }}
						/>
					</div>

					<div style={{ marginBottom: '12px' }}>
						<label htmlFor="mail-cliente"><strong>Mail</strong></label>
						<input
							id="mail-cliente"
							type="email"
							value={mail}
							onChange={(event) => setMail(event.target.value)}
							style={{ width: '100%', marginTop: '6px', padding: '8px' }}
						/>
					</div>

					<div style={{ marginBottom: '12px' }}>
						<label htmlFor="telefono-cliente"><strong>Telefono</strong></label>
						<input
							id="telefono-cliente"
							type="text"
							value={telefono}
							onChange={handleTelefonoChange}
							style={{ width: '100%', marginTop: '6px', padding: '8px' }}
						/>
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

export default EditarCliente;
