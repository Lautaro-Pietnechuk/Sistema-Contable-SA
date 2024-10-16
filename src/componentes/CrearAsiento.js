import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode'; // Importación con llaves
import './CrearAsiento.css';

const CrearAsiento = () => {
    const [descripcion, setDescripcion] = useState('');
    const [movimientos, setMovimientos] = useState([{ cuenta: '', asiento: '', tipo: 'debe', monto: '', saldo: '', saldoActual: 0 }]);
    const [cuentas, setCuentas] = useState([]);
    const [token, setToken] = useState('');
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const navigate = useNavigate();
    const fechaActual = new Date().toISOString().slice(0, 10);

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
            cargarCuentas(storedToken);
        }
    }, [navigate]);

    const cargarCuentas = async (token) => {
        try {
            const response = await axios.get('http://localhost:8080/api/cuentas?recibeSaldo=true', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const cuentasConSaldo = response.data.filter(cuenta => cuenta.recibeSaldo);
            setCuentas(cuentasConSaldo);
        } catch (error) {
            console.error('Error al cargar las cuentas:', error);
            setMensajeError('Error al cargar las cuentas.');
        }
    };

    const obtenerUsuarioDelToken = () => {
        try {
            const token = localStorage.getItem('token');
            const decoded = jwtDecode(token);
            return decoded.usuarioId || 1;
        } catch (error) {
            console.error('Error al decodificar el token:', error);
            return 1;
        }
    };

    const manejarEnvio = async (e) => {
        e.preventDefault();

        const saldoNegativo = movimientos.some(movimiento => movimiento.saldo < 0);
        if (saldoNegativo) {
            setMensajeError('No se puede hacer el movimiento porque el saldo calculado es negativo.');
            return;
        }

        const nuevoAsiento = {
            fecha: fechaActual,
            descripcion,
            id_usuario: obtenerUsuarioDelToken(),
            cuentasAsientos: movimientos.map(mov => ({
                cuenta: mov.cuenta,
                asiento: mov.asiento,
                debe: mov.tipo === 'debe' ? parseFloat(mov.monto) : 0,
                haber: mov.tipo === 'haber' ? parseFloat(mov.monto) : 0,
                saldo: mov.saldo
            }))
        };

        try {
            await axios.post('http://localhost:8080/api/asientos/crear', nuevoAsiento, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Asiento creado con éxito.');
            setTimeout(() => setMensajeExito(''), 3000);
            setDescripcion('');
            setMovimientos([{ cuenta: '', asiento: '', tipo: 'debe', monto: '', saldo: '', saldoActual: 0 }]);
        } catch (error) {
            console.error('Error al crear el asiento:', error);
            const mensaje = error.response?.data?.message || 'Error al crear el asiento. Por favor, inténtelo de nuevo.';
            setMensajeError(mensaje);
        }
    };

    const agregarMovimiento = () => {
        setMovimientos([...movimientos, { cuenta: '', asiento: '', tipo: 'debe', monto: '', saldo: '', saldoActual: 0 }]);
    };

    const manejarMovimientoChange = (index, field, value) => {
        const nuevosMovimientos = [...movimientos];

        if (field === 'cuenta') {
            const cuentaSeleccionada = cuentas.find(c => c.codigo === value);
            if (cuentaSeleccionada) {
                nuevosMovimientos[index].saldoActual = cuentaSeleccionada.saldoActual;
            }
            nuevosMovimientos[index].cuenta = value;
        }

        if (field === 'monto' && (!isNaN(value) && parseFloat(value) >= 0)) {
            nuevosMovimientos[index].monto = value;
            nuevosMovimientos[index].saldo = 
                nuevosMovimientos[index].tipo === 'debe'
                    ? nuevosMovimientos[index].saldoActual - parseFloat(value)
                    : nuevosMovimientos[index].saldoActual + parseFloat(value);
        }

        setMovimientos(nuevosMovimientos);
    };

    return (
        <div>
            <h2>Crear Nuevo Asiento</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>}
            <form onSubmit={manejarEnvio}>
                <div>
                    <label>Fecha:</label>
                    <p>{fechaActual}</p>
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
                {movimientos.map((mov, index) => (
                    <div key={index} className="movimiento">
                        {/* Campos de selección y entrada */}
                    </div>
                ))}
                <button type="button" onClick={agregarMovimiento}>Agregar Movimiento</button>
                <button type="submit">Crear Asiento</button>
            </form>
        </div>
    );
};

export default CrearAsiento;
