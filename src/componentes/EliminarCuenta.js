import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const EliminarCuenta = () => {
    const [cuentas, setCuentas] = useState([]);
    const [cuentaSeleccionada, setCuentaSeleccionada] = useState('');
    const [asientos, setAsientos] = useState([]);
    const [asientoSeleccionado, setAsientoSeleccionado] = useState('');
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
            obtenerAsientos(storedToken);
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

    const obtenerAsientos = async (token) => {
        try {
            const respuesta = await axios.get('http://localhost:8080/api/asientos/listar', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setAsientos(respuesta.data);
        } catch (error) {
            console.error('Error al obtener asientos:', error);
            setMensajeError('Error al obtener asientos.');
        }
    };

    const manejarEnvioCuenta = async (e) => {
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
            setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                if (error.response.status === 403) {
                    setMensajeError('Necesitas permisos de administrador para poder eliminar una cuenta.');
                } else {
                    setMensajeError(`Error: ${error.response.data.error || error.response.data.message}`);
                }
            } else {
                console.error('Error al eliminar la cuenta:', error.message);
                setMensajeError('Error desconocido al eliminar la cuenta.');
            }
        }
    };

    const manejarEnvioAsiento = async (e) => {
        e.preventDefault();
        if (!asientoSeleccionado) {
            setMensajeError('Seleccione un asiento para eliminar.');
            return;
        }

        try {
            await axios.delete(`http://localhost:8080/api/asientos/${asientoSeleccionado}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Asiento eliminado con éxito.');
            setMensajeError('');
            setAsientoSeleccionado('');
            obtenerAsientos(token); // Actualizar la lista de asientos
            setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                if (error.response.status === 403) {
                    setMensajeError('Necesitas permisos de administrador para poder eliminar un asiento.');
                } else {
                    setMensajeError(`Error: ${error.response.data.error || error.response.data.message}`);
                }
            } else {
                console.error('Error al eliminar el asiento:', error.message);
                setMensajeError('Error desconocido al eliminar el asiento.');
            }
        }
    };

    return (
        <div>
            <h2>Eliminar Cuenta y Asiento</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>}

            {/* Formulario para eliminar cuentas */}
            <form onSubmit={manejarEnvioCuenta}>
                <div>
                    <label htmlFor="cuenta">Seleccionar cuenta a eliminar:</label>
                    <select
                        id="cuenta"
                        value={cuentaSeleccionada}
                        onChange={(e) => setCuentaSeleccionada(e.target.value)}
                        required
                    >
                        <option value="">Seleccionar cuenta</option>
                        {cuentas.map((cuenta) => (
                            <option key={cuenta.id} value={cuenta.id}>
                                {cuenta.nombre} - {cuenta.codigo}
                            </option>
                        ))}
                    </select>
                </div>
                <button type="submit">Eliminar Cuenta</button>
            </form>

            {/* Formulario para eliminar asientos */}
            <form onSubmit={manejarEnvioAsiento}>
                <div>
                    <label htmlFor="asiento">Seleccionar asiento a eliminar:</label>
                    <select
                        id="asiento"
                        value={asientoSeleccionado}
                        onChange={(e) => setAsientoSeleccionado(e.target.value)}
                        required
                    >
                        <option value="">Seleccionar asiento</option>
                        {asientos.map((asiento) => (
                            <option key={asiento.id} value={asiento.id}>
                                {asiento.descripcion}
                            </option>
                        ))}
                    </select>
                </div>
                <button type="submit">Eliminar Asiento</button>
            </form>
        </div>
    );
};

export default EliminarCuenta;
