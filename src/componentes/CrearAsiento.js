import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode'; 
import './CrearAsiento.css';

const CrearAsiento = () => {
    const [descripcion, setDescripcion] = useState('');
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const [cuentas, setCuentas] = useState([]);
    const [cuentaSeleccionada, setCuentaSeleccionada] = useState('');
    const [tipoMovimiento, setTipoMovimiento] = useState('debe');
    const [monto, setMonto] = useState('');
    const [saldoCuenta, setSaldoCuenta] = useState(0);
    const [asientos, setAsientos] = useState([]);
    const [asientoSeleccionado, setAsientoSeleccionado] = useState('');
    const [saldoResultado, setSaldoResultado] = useState(0); // Nuevo estado para el saldo resultante
    const navigate = useNavigate();
    const fechaActual = new Date().toISOString().slice(0, 10);

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            cargarCuentas(storedToken);
            cargarAsientos(storedToken);
        }
    }, [navigate]);

    const cargarCuentas = async (token) => {
        try {
            const respuesta = await axios.get('http://localhost:8080/api/cuentas/recibeSaldo', {
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

    const cargarAsientos = async (token) => {
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

    const obtenerUsuarioDelToken = () => {
        try {
            const storedToken = localStorage.getItem('token');
            const decoded = jwtDecode(storedToken);
            return decoded.usuarioId || 1;
        } catch (error) {
            console.error('Error al decodificar el token:', error);
            return 1;
        }
    };

    const manejarEnvioAsiento = async (e) => {
        e.preventDefault();
        const nuevoAsiento = {
            fecha: fechaActual,
            descripcion,
        };
    
        try {
            const storedToken = localStorage.getItem('token');
            const idUsuario = obtenerUsuarioDelToken();
            await axios.post(`http://localhost:8080/api/asientos/crear/${idUsuario}`, nuevoAsiento, {
                headers: { Authorization: `Bearer ${storedToken}` }
            });
            setMensajeExito('Asiento creado con éxito.');
            setDescripcion('');
            cargarAsientos(storedToken);
        } catch (error) {
            console.error('Error al crear el asiento:', error);
            const mensaje = error.response?.data?.message || 'Error al crear el asiento.';
            setMensajeError(mensaje);
            setTimeout(() => setMensajeError(''), 3000);
        }
    };

    const manejarEnvioMovimiento = async (e) => {
        e.preventDefault();
        const nuevoMovimiento = {
            cuenta_codigo: cuentaSeleccionada,
            tipo: tipoMovimiento,
            monto: parseFloat(monto),
            asiento_id: asientoSeleccionado,
        };

        try {
            const storedToken = localStorage.getItem('token');
            await axios.post('http://localhost:8080/api/movimientos/crear', nuevoMovimiento, {
                headers: { Authorization: `Bearer ${storedToken}` }
            });
            const nuevoSaldo = tipoMovimiento === 'debe' ? saldoCuenta + parseFloat(monto) : saldoCuenta - parseFloat(monto);
            setSaldoCuenta(nuevoSaldo);
            setMensajeExito('Movimiento creado con éxito.');
            setTimeout(() => setMensajeExito(''), 3000);
            setCuentaSeleccionada('');
            setTipoMovimiento('debe');
            setMonto('');
            setAsientoSeleccionado('');
            setSaldoResultado(0); // Reinicia el saldo resultante
        } catch (error) {
            console.error('Error al crear el movimiento:', error);
            const mensaje = error.response?.data?.message || 'Error al crear el movimiento.';
            setMensajeError(mensaje);
            setTimeout(() => setMensajeError(''), 3000);
        }
    };

    const handleCuentaChange = (e) => {
        const cuenta = e.target.value;
        setCuentaSeleccionada(cuenta);
        obtenerSaldoCuenta(cuenta);
    };

    const obtenerSaldoCuenta = async (cuentaCodigo) => {
        try {
            const storedToken = localStorage.getItem('token');
            const response = await axios.get(`http://localhost:8080/api/cuentas/${cuentaCodigo}/saldo`, {
                headers: { Authorization: `Bearer ${storedToken}` }
            });
            setSaldoCuenta(response.data.saldo);
        } catch (error) {
            console.error('Error al obtener saldo de la cuenta:', error);
            const mensaje = error.response?.data?.message || 'Error al obtener saldo.';
            setMensajeError(mensaje);
            setTimeout(() => setMensajeError(''), 3000);
        }
    };

    const calcularSaldoResultado = () => {
        if (monto) {
            const montoNumerico = parseFloat(monto);
            const nuevoSaldo = tipoMovimiento === 'debe' ? saldoCuenta + montoNumerico : saldoCuenta - montoNumerico;
            setSaldoResultado(nuevoSaldo);
        } else {
            setSaldoResultado(0);
        }
    };

    useEffect(() => {
        calcularSaldoResultado(); // Calcular el saldo resultante cada vez que se cambia el monto o el tipo de movimiento
    }, [monto, tipoMovimiento, saldoCuenta]);

    return (
        <div className="crear-asiento">
            <h2>Crear Asiento</h2>
            <form onSubmit={manejarEnvioAsiento}>
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
                <button type="submit">Crear Asiento</button>
            </form>
            {mensajeExito && <p className="success-message">{mensajeExito}</p>}
            {mensajeError && <p className="error-message">{mensajeError}</p>}
            
            <h2>Crear Movimiento</h2>
            <form onSubmit={manejarEnvioMovimiento}>
                {/* Selector de Asientos */}
                <div>
                    <label htmlFor="asiento">Seleccionar Asiento:</label>
                    <select
                        id="asiento"
                        value={asientoSeleccionado}
                        onChange={(e) => setAsientoSeleccionado(e.target.value)}
                        required
                    >
                        <option value="">Seleccione un asiento</option>
                        {asientos.map((asiento) => (
                            <option key={asiento.id} value={asiento.id}>
                                {asiento.descripcion} {/* Solo mostrar la descripción */}
                            </option>
                        ))}
                    </select>
                </div>
                
                {/* Selector de Cuenta */}
                <div>
                    <label htmlFor="cuenta">Seleccionar Cuenta:</label>
                    <select
                        id="cuenta"
                        value={cuentaSeleccionada}
                        onChange={handleCuentaChange}
                        required
                    >
                        <option value="">Seleccione una cuenta</option>
                        {cuentas.map((cuenta) => (
                            <option key={cuenta.codigo} value={cuenta.codigo}>
                                {cuenta.nombre}
                            </option>
                        ))}
                    </select>
                </div>
                
                {/* Resto del formulario de crear movimiento */}
                <div>
                    <label htmlFor="tipoMovimiento">Tipo de Movimiento:</label>
                    <select
                        id="tipoMovimiento"
                        value={tipoMovimiento}
                        onChange={(e) => setTipoMovimiento(e.target.value)}
                    >
                        <option value="debe">Debe</option>
                        <option value="haber">Haber</option>
                    </select>
                </div>
                <div>
                    <label htmlFor="monto">Monto:</label>
                    <input
                        type="number"
                        id="monto"
                        value={monto}
                        onChange={(e) => setMonto(e.target.value)}
                        required
                    />
                </div>
                {/* Mostrar el saldo resultante */}
                <div>
                    <p>Saldo Resultante: {saldoResultado}</p>
                </div>
                <button type="submit">Crear Movimiento</button>
            </form>
            {mensajeExito && <p className="success-message">{mensajeExito}</p>}
            {mensajeError && <p className="error-message">{mensajeError}</p>}
        </div>
    );
};

export default CrearAsiento;
