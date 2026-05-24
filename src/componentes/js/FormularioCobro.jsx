import React, { useState, useEffect } from 'react';
import axios from 'axios';

const FormularioCobro = ({ onCobroExitoso }) => {
    const [clientes, setClientes] = useState([]);
    const [cobro, setCobro] = useState({
        clienteId: '',
        monto: '',
        metodoPago: 'EFECTIVO', 
        observaciones: ''
    });
    const [error, setError] = useState('');
    const [mensajeExito, setMensajeExito] = useState(''); // NUEVO: Estado para el mensaje de éxito
    const [cargando, setCargando] = useState(false); 

    useEffect(() => {
        const obtenerClientes = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/clientes');
                setClientes(response.data);
            } catch (err) {
                console.error("Error al cargar clientes en formulario:", err);
                setError('No se pudieron cargar los clientes del sistema.');
            }
        };
        obtenerClientes();
    }, []);

    const handleChange = (e) => {
        setCobro({ ...cobro, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!cobro.clienteId || !cobro.monto || parseFloat(cobro.monto) <= 0) {
            setError('Por favor, selecciona un cliente y un monto válido.');
            return;
        }

        try {
            setError('');
            setMensajeExito(''); // Limpiar éxitos anteriores si los hubiera
            setCargando(true);

            await axios.post('http://localhost:8080/api/cobros', {
                clienteId: parseInt(cobro.clienteId),
                monto: parseFloat(cobro.monto),
                metodoPago: cobro.metodoPago,
                observaciones: cobro.observaciones
            });

            // CORREGIDO: Se reemplaza el alert() por el mensaje de estado en línea
            setMensajeExito('Cobro registrado correctamente.');
            setCobro({ clienteId: '', monto: '', metodoPago: 'EFECTIVO', observaciones: '' });
            
            if (onCobroExitoso) onCobroExitoso(); 

            // Desaparece automáticamente a los 3 segundos (igual que en RegistrarVenta)
            setTimeout(() => setMensajeExito(''), 3000);

        } catch (err) {
            console.error("Error en el POST de cobro:", err);
            setError('Error al registrar el cobro en el servidor. Verifica los datos.');
        } finally {
            setCargando(false);
        }
    };

    return (
        <div style={{ maxWidth: '500px', margin: '20px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '4px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginTop: 0, marginBottom: '20px', borderBottom: '1px solid #eee', paddingBottom: '10px', color: '#333' }}>
                Registrar Nuevo Cobro
            </h3>
            
            {error && <p style={{ color: '#d9534f', fontSize: '14px', backgroundColor: '#f2dede', padding: '10px', borderRadius: '4px', border: '1px solid #ebccd1' }}>{error}</p>}
            
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px', color: '#555' }}>Cliente:</label>
                    <select name="clienteId" value={cobro.clienteId} onChange={handleChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box', backgroundColor: '#fff' }}>
                        <option value="">-- Seleccionar Cliente --</option>
                        {clientes.map(c => (
                            <option key={c.id} value={c.id}>{c.nombre}</option>
                        ))}
                    </select>
                </div>

                <div>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px', color: '#555' }}>Monto ($):</label>
                    <input type="number" name="monto" value={cobro.monto} onChange={handleChange} placeholder="0.00" step="0.01" style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box' }} />
                </div>

                <div>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px', color: '#555' }}>Medio de Pago:</label>
                    <select name="metodoPago" value={cobro.metodoPago} onChange={handleChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box', backgroundColor: '#fff' }}>
                        <option value="EFECTIVO">Efectivo</option>
                        <option value="DEBITO">Transferencia / Débito</option>
                        <option value="CREDITO">Tarjeta de Crédito</option>
                    </select>
                </div>

                <div>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px', color: '#555' }}>Observaciones (Opcional):</label>
                    <textarea name="observaciones" value={cobro.observaciones} onChange={handleChange} placeholder="Ej: Abona saldo pendiente de factura A" style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box', resize: 'vertical', minHeight: '60px' }} />
                </div>

                <button 
                    type="submit" 
                    disabled={cargando}
                    style={{ 
                        width: '100%', 
                        padding: '12px', 
                        backgroundColor: cargando ? '#ccc' : '#28a745', 
                        color: '#fff',
                        border: 'none', 
                        borderRadius: '4px', 
                        cursor: cargando ? 'not-allowed' : 'pointer', 
                        fontWeight: 'bold', 
                        marginTop: '10px',
                        fontSize: '16px',
                        transition: 'background-color 0.2s ease'
                    }}
                >
                    {cargando ? 'Registrando...' : 'Registrar Cobro'}
                </button>
            </form>

            {/* MODIFICADO: Mensaje de éxito renderizado al pie del contenedor con la misma firma visual que RegistrarVenta */}
            {mensajeExito && <p style={{ color: '#28a745', textAlign: 'center', marginTop: '15px', fontWeight: 'bold' }}>{mensajeExito}</p>}
        </div>
    );
};

export default FormularioCobro;