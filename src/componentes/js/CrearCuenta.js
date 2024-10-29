import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../css/CrearCuenta.css';
import { useNavigate } from 'react-router-dom';

const CrearCuenta = () => {
    const [nombre, setNombre] = useState('');
    const [tipo, setTipo] = useState('');
    const [recibeSaldo, setRecibeSaldo] = useState(true); // Establece recibeSaldo como true siempre
    const [codigo, setCodigo] = useState('');
    const [token, setToken] = useState('');
    const [mensajeExito, setMensajeExito] = useState(''); // Estado para el mensaje de éxito
    const [mensajeError, setMensajeError] = useState(''); // Estado para el mensaje de error
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

        // Validar el código según el tipo de cuenta
        if (tipo === 'Activo' && !codigo.startsWith('1')) {
            setMensajeError('El código para una cuenta de tipo Activo debe comenzar con 1.');
            return;
        }
        if (tipo === 'Pasivo' && !codigo.startsWith('2')) {
            setMensajeError('El código para una cuenta de tipo Pasivo debe comenzar con 2.');
            return;
        }
        if (tipo === 'Patrimonio' && !codigo.startsWith('3')) {
            setMensajeError('El código para una cuenta de tipo Patrimonio debe comenzar con 3.');
            return;
        }
        if (tipo === 'Ingreso' && !codigo.startsWith('4')) {
            setMensajeError('El código para una cuenta de tipo Ingreso debe comenzar con 4.');
            return;
        }
        if (tipo === 'Egreso' && !codigo.startsWith('5')) {
            setMensajeError('El código para una cuenta de tipo Egreso debe comenzar con 5.');
            return;
        }

        const nuevaCuenta = { nombre, tipo, codigo, recibeSaldo }; // Siempre será true

        try {
            await axios.post('http://localhost:8080/api/cuentas/crear', nuevaCuenta, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            setMensajeExito('Cuenta creada con éxito.');
            setMensajeError(''); // Limpiar mensaje de error
            setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
            // Limpiar formulario
            setNombre('');
            setTipo('');
            setCodigo('');
            setRecibeSaldo(true); // Mantiene siempre recibeSaldo como true
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                // Aquí se puede verificar el código de estado o el mensaje del error
                if (error.response.status === 401) {
                    alert('Sesión expirada. Por favor, inicie sesión nuevamente.');
                    localStorage.removeItem('token');
                    navigate('/login');
                } else if (error.response.status === 403) {
                    setMensajeError('Necesitas permisos de administrador para poder crear una cuenta.');
                } else {
                    alert(`Error: ${error.response.status} - ${error.response.data.error || error.response.data.message}`);
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

    const modificarCodigo = (tipoSeleccionado) => {
        switch (tipoSeleccionado) {
            case 'Activo':
                return '1' + codigo.slice(1); // Cambiar solo el primer dígito a '1'
            case 'Pasivo':
                return '2' + codigo.slice(1); // Cambiar solo el primer dígito a '2'
            case 'Patrimonio':
                return '3' + codigo.slice(1); // Cambiar solo el primer dígito a '3'
            case 'Ingreso':
                return '4' + codigo.slice(1); // Establecer a '400'
            case 'Egreso':
                return '5' + codigo.slice(1); // Establecer a '500'
            default:
                return codigo; // Retornar el código sin cambios si no es un tipo válido
        }
    };

    const handleTipoChange = (e) => {
        const selectedTipo = e.target.value;
        setTipo(selectedTipo);
        const nuevoCodigo = modificarCodigo(selectedTipo);
        setCodigo(nuevoCodigo); // Cambia el código al nuevo código basado en el tipo seleccionado
    };

    const handleCodigoChange = (e) => {
        const newCodigo = e.target.value;
        setCodigo(newCodigo);
        // No se necesita actualizar recibeSaldo aquí
    };

    return (
        <div>
            <h2>Agregar Nueva Cuenta</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>} {/* Mostrar mensaje de éxito */}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>} {/* Mostrar mensaje de error */}
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
                        <option value="Patrimonio">Patrimonio</option>
                        <option value="Ingreso">Ingreso</option>
                        <option value="Egreso">Egreso</option>
                    </select>
                </div>
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
                <button type="submit">Agregar Cuenta</button>
            </form>
        </div>
    );
};

export default CrearCuenta;
