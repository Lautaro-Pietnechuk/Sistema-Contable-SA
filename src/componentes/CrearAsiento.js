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
    const [monto, setMonto] = useState('');
    const [saldoCuenta, setSaldoCuenta] = useState(0);
    const [asientos, setAsientos] = useState([]);
    const [asientoSeleccionado, setAsientoSeleccionado] = useState('');
    const [saldoResultado, setSaldoResultado] = useState(0);
    const [esDebe, setEsDebe] = useState(true); // Control entre 'Debe' y 'Haber'
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
                headers: { Authorization: `Bearer ${token}` },
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
                headers: { Authorization: `Bearer ${token}` },
            });
            console.log('Asientos cargados:', respuesta.data); // Log para verificar la respuesta
            setAsientos(respuesta.data || []);
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
        if (!descripcion.trim()) {
            setMensajeError('La descripción no puede estar vacía.');
            return;
        }

        const nuevoAsiento = { descripcion, fecha: fechaActual };
        try {
            const token = localStorage.getItem('token');
            await axios.post('http://localhost:8080/api/asientos/crear', nuevoAsiento, {
                headers: { Authorization: `Bearer ${token}` },
            });

            setMensajeExito('Asiento creado con éxito.');
            setDescripcion('');
            cargarAsientos(token);
        } catch (error) {
            console.error('Error al crear el asiento:', error);
            setMensajeError('Error al crear el asiento.');
        }
    };

    const manejarEnvioMovimiento = async (e) => {
        e.preventDefault();
    
        // Agregar log para verificar el valor de asientoSeleccionado
        console.log('Asiento seleccionado:', asientoSeleccionado);
    
        if (!asientoSeleccionado) {
            setMensajeError('Debe seleccionar un asiento válido.');
            setTimeout(() => setMensajeError(''), 3000);
            return;
        }
    
        if (!monto || parseFloat(monto) <= 0) {
            setMensajeError('El monto debe ser mayor a cero.');
            setTimeout(() => setMensajeError(''), 3000);
            return;
        }
    
        const nuevoMovimiento = {
            cuentaCodigo: parseInt(cuentaSeleccionada, 10),
            debe: esDebe ? parseFloat(monto) : 0,
            haber: esDebe ? 0 : parseFloat(monto),
            asientoId: parseInt(asientoSeleccionado, 10), // Asegúrate de que aquí se envía el ID correcto
            saldo: saldoResultado,
        };
    
        console.log('Movimiento a enviar:', nuevoMovimiento);
    
        try {
            const storedToken = localStorage.getItem('token');
            await axios.post('http://localhost:8080/api/movimientos/crear', nuevoMovimiento, {
                headers: { Authorization: `Bearer ${storedToken}` },
            });
    
            setMensajeExito('Movimiento creado con éxito.');
            setTimeout(() => setMensajeExito(''), 3000);
    
            // Limpiar los campos del formulario después de enviar
            setCuentaSeleccionada('');
            setMonto('');
            setAsientoSeleccionado('');
            setSaldoResultado(0);
    
            cargarAsientos(storedToken);
        } catch (error) {
            console.error('Error al crear el movimiento:', error);
            setMensajeError(error.response?.data?.message || 'Error al crear el movimiento.');
            setTimeout(() => setMensajeError(''), 3000);
        }
    };
    
    const obtenerSaldoCuenta = async (cuentaCodigo) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`http://localhost:8080/api/cuentas/${cuentaCodigo}/saldo`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setSaldoCuenta(response.data.saldo);
        } catch (error) {
            console.error('Error al obtener saldo:', error);
            setMensajeError('Error al obtener saldo.');
        }
    };

    const calcularSaldoResultado = () => {
        const montoNumerico = parseFloat(monto) || 0;
        const nuevoSaldo = esDebe ? saldoCuenta + montoNumerico : saldoCuenta - montoNumerico;
        setSaldoResultado(nuevoSaldo);
    };

    useEffect(() => {
        calcularSaldoResultado();
    }, [monto, esDebe, saldoCuenta]);

    const handleCuentaChange = (e) => {
        const cuenta = e.target.value;
        setCuentaSeleccionada(cuenta);
        obtenerSaldoCuenta(cuenta);
    };

    return (
        <div className="crear-asiento">
            <h2>Crear Asiento</h2>
            <form onSubmit={manejarEnvioAsiento}>
                <label htmlFor="descripcion">Descripción:</label>
                <input
                    type="text"
                    id="descripcion"
                    value={descripcion}
                    onChange={(e) => setDescripcion(e.target.value)}
                    required
                />
                <button type="submit">Crear Asiento</button>
            </form>

            <h2>Crear Movimiento</h2>
            <form onSubmit={manejarEnvioMovimiento}>
                <label htmlFor="asiento">Seleccionar Asiento:</label>
                <select
                    id="asiento"
                    value={asientoSeleccionado}
                    onChange={(e) => {
                        console.log('Asiento seleccionado:', e.target.value); // Log para verificar el valor seleccionado
                        setAsientoSeleccionado(e.target.value);
                    }}
                    required
                >
                    <option value="">Seleccione un asiento</option>
                    {asientos.map((asiento) => (
                        <option key={asiento.id} value={asiento.id}>
                            {asiento.descripcion}
                        </option>
                    ))}
                </select>

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

                <div>
                    <label>Tipo de Movimiento:</label>
                    <label>
                        <input type="radio" checked={esDebe} onChange={() => setEsDebe(true)} /> Debe
                    </label>
                    <label>
                        <input type="radio" checked={!esDebe} onChange={() => setEsDebe(false)} /> Haber
                    </label>
                </div>

                <label htmlFor="monto">Monto:</label>
                <input
                    type="number"
                    id="monto"
                    value={monto}
                    onChange={(e) => setMonto(e.target.value)}
                    required
                />

                <p>Saldo Resultante: {saldoResultado}</p>
                <button type="submit">Crear Movimiento</button>
            </form>

            {mensajeExito && <p className="exito">{mensajeExito}</p>}
            {mensajeError && <p className="error">{mensajeError}</p>}
        </div>
    );
};

export default CrearAsiento;
