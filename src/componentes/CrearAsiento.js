import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './CrearAsiento.css'; // Asegúrate de tener estilos si es necesario

const CrearAsiento = () => {
    const [fecha, setFecha] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [movimientos, setMovimientos] = useState([{ cuenta: '', monto: '', tipo: 'Debito' }]);
    const [token, setToken] = useState('');
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
        }
    }, [navigate]);

    const manejarEnvio = async (e) => {
        e.preventDefault();

        const nuevoAsiento = {
            fecha,
            descripcion,
            // Agregar el ID del usuario en base a la sesión actual si es necesario
            id_usuario: 1, // Este debe ser dinámico en un caso real
            cuentasAsientos: movimientos.map(movimiento => ({
                cuenta: movimiento.cuenta,
                monto: movimiento.monto,
                tipo: movimiento.tipo
            }))
        };

        try {
            await axios.post('http://localhost:8080/api/asientos/crear', nuevoAsiento, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Asiento creado con éxito.');
            setMensajeError('');
            setTimeout(() => setMensajeExito(''), 3000);
            setFecha('');
            setDescripcion('');
            setMovimientos([{ cuenta: '', monto: '', tipo: 'Debito' }]);
        } catch (error) {
            console.error('Error al crear el asiento:', error);
            setMensajeError('Error al crear el asiento. Por favor, inténtelo de nuevo.');
        }
    };

    const agregarMovimiento = () => {
        setMovimientos([...movimientos, { cuenta: '', monto: '', tipo: 'Debito' }]);
    };

    const manejarMovimientoChange = (index, field, value) => {
        const nuevosMovimientos = [...movimientos];
        nuevosMovimientos[index][field] = value;
        setMovimientos(nuevosMovimientos);
    };

    return (
        <div>
            <h2>Crear Nuevo Asiento</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>}
            <form onSubmit={manejarEnvio}>
                <div>
                    <label htmlFor="fecha">Fecha:</label>
                    <input
                        type="date"
                        id="fecha"
                        value={fecha}
                        onChange={(e) => setFecha(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="descripcion">Descripción:</label>
                    <input
                        type="text"
                        id="descripcion"
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                        required
                    />
                </div>
                <h3>Movimientos</h3>
                {movimientos.map((movimiento, index) => (
                    <div key={index} className="movimiento">
                        <label htmlFor={`cuenta-${index}`}>Cuenta:</label>
                        <input
                            type="text"
                            id={`cuenta-${index}`}
                            value={movimiento.cuenta}
                            onChange={(e) => manejarMovimientoChange(index, 'cuenta', e.target.value)}
                            required
                        />
                        <label htmlFor={`monto-${index}`}>Monto:</label>
                        <input
                            type="number"
                            id={`monto-${index}`}
                            value={movimiento.monto}
                            onChange={(e) => manejarMovimientoChange(index, 'monto', e.target.value)}
                            required
                        />
                        <label htmlFor={`tipo-${index}`}>Tipo:</label>
                        <select
                            id={`tipo-${index}`}
                            value={movimiento.tipo}
                            onChange={(e) => manejarMovimientoChange(index, 'tipo', e.target.value)}
                        >
                            <option value="Debito">Débito</option>
                            <option value="Credito">Crédito</option>
                        </select>
                    </div>
                ))}
                <button type="button" onClick={agregarMovimiento}>Agregar Movimiento</button>
                <button type="submit">Crear Asiento</button>
            </form>
        </div>
    );
};

export default CrearAsiento;
