import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const EliminarCuenta = () => {
    const [cuentas, setCuentas] = useState([]);
    const [cuentaSeleccionada, setCuentaSeleccionada] = useState('');
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const [token, setToken] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
            obtenerCuentas(storedToken);
        }
    }, [navigate]);

    const obtenerCuentas = async (token) => {
        try {
            const respuesta = await axios.get('http://localhost:8080/api/cuentas', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setCuentas(respuesta.data);
        } catch (error) {
            console.error('Error al obtener cuentas:', error);
            setMensajeError('Error al obtener cuentas.');
        }
    };

    const manejarEnvio = async (e) => {
        e.preventDefault();
        if (!cuentaSeleccionada) {
            setMensajeError('Seleccione una cuenta para eliminar.');
            return;
        }

        try {
            await axios.delete(`http://localhost:8080/api/cuentas/${cuentaSeleccionada}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Cuenta eliminada con éxito.');
            setMensajeError('');
            setCuentaSeleccionada('');
            obtenerCuentas(token); // Actualizar la lista de cuentas
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                setMensajeError(`Error: ${error.response.data.error || error.response.data.message}`);
            } else {
                console.error('Error al eliminar la cuenta:', error.message);
                setMensajeError('Error desconocido al eliminar la cuenta.');
            }
        }
    };

    return (
        <div>
            <h2>Eliminar Cuenta</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>} {/* Mostrar mensaje de éxito */}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>} {/* Mostrar mensaje de error */}
            <form onSubmit={manejarEnvio}>
                <div>
                    <label htmlFor="cuenta">Selecciona una cuenta a eliminar:</label>
                    <select
                        id="cuenta"
                        value={cuentaSeleccionada}
                        onChange={(e) => setCuentaSeleccionada(e.target.value)}
                        required
                    >
                        <option value="">Seleccionar cuenta</option>
                        {cuentas.map((cuenta) => (
                            <option key={cuenta.codigo} value={cuenta.codigo}>
                                {cuenta.nombre} (Código: {cuenta.codigo})
                            </option>
                        ))}
                    </select>
                </div>
                <button type="submit">Eliminar Cuenta</button>
            </form>
        </div>
    );
};

export default EliminarCuenta;
