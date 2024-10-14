import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AgregarCuentas.css';
import { useNavigate } from 'react-router-dom';

const AgregarCuentas = () => {
    const [nombre, setNombre] = useState('');
    const [tipo, setTipo] = useState('');
    const [recibeSaldo, setRecibeSaldo] = useState(false);
    const [codigo, setCodigo] = useState('');
    const [token, setToken] = useState('');
    const [mensajeExito, setMensajeExito] = useState(''); // Estado para el mensaje de éxito
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
        const nuevaCuenta = { nombre, tipo, codigo, recibeSaldo };
        
        try {
            await axios.post('http://localhost:8080/api/cuentas/crear', nuevaCuenta, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Cuenta creada con éxito.');
            setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
            setNombre('');
            setTipo('');
            setCodigo('');
            setRecibeSaldo(false);
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                alert(`Error: ${error.response.status} - ${error.response.data.error || error.response.data.message}`);
                if (error.response.status === 401) {
                    alert('Sesión expirada. Por favor, inicie sesión nuevamente.');
                    localStorage.removeItem('token');
                    navigate('/login');
                }
            } else if (error.request) {
                console.error('No se recibió respuesta del servidor:', error.request);
                alert('No se pudo conectar con el servidor.');
            } else {
                console.error('Error al configurar la solicitud:', error.message);
                alert('Error desconocido al agregar la cuenta.');
            }
        }
    };

    const handleTipoChange = (e) => {
        const selectedTipo = e.target.value;
        setTipo(selectedTipo);
        setCodigo(selectedTipo === 'Activo' ? 100 : 200);
    };

    const handleCodigoChange = (e) => {
        const newCodigo = e.target.value;
        setCodigo(newCodigo);
        setRecibeSaldo(newCodigo.length >= 3 && newCodigo.slice(-1) !== '0');
    };

    return (
        <div>
            <h2>Agregar Nueva Cuenta</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>} {/* Mostrar mensaje de éxito */}
            <form onSubmit={manejarEnvio}>
                <div>
                    <label htmlFor="nombre">Nombre:</label>
                    <input
                        type="text"
                        id="nombre"
                        value={nombre}
                        onChange={(e) => setNombre(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="tipo">Tipo:</label>
                    <select id="tipo" value={tipo} onChange={handleTipoChange} required>
                        <option value="">Seleccionar tipo</option>
                        <option value="Activo">Activo</option>
                        <option value="Pasivo">Pasivo</option>
                    </select>
                </div>
                <div className="codigo-recibe-saldo">
                    <div>
                        <label htmlFor="codigo">Código:</label>
                        <input
                            type="text"
                            id="codigo"
                            value={codigo}
                            onChange={handleCodigoChange}
                            required
                        />
                    </div>
                    <div className="recibe-saldo">
                        <label>
                            <input
                                type="checkbox"
                                checked={recibeSaldo}
                                readOnly
                                className="blocked-checkbox"
                            />
                            ¿Recibe Saldo?
                        </label>
                    </div>
                </div>
                <button type="submit">Agregar Cuenta</button>
            </form>
        </div>
    );
};

export default AgregarCuentas;
