import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../css/EditarCuenta.css'

const EditarCuenta = () => {
    const [cuentas, setCuentas] = useState([]);
    const [codigo, setCodigo] = useState('');
    const [nuevoNombre, setNuevoNombre] = useState('');
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchCuentas = async () => {
            try {
                const token = localStorage.getItem('token');
                const response = await axios.get('http://localhost:8080/api/cuentas', {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                setCuentas(response.data);
            } catch (error) {
                console.error('Error al cargar las cuentas:', error);
                setError('Error al cargar las cuentas.');
            }
        };
        fetchCuentas();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');
        if (!nuevoNombre.trim()) {
            setError('El nuevo nombre no puede estar vacío.');
            return;
        }
        try {
            const token = localStorage.getItem('token'); // Recupera el token
            console.log('Token:', token); // Verifica que el token se está obteniendo correctamente
            
            const response = await axios.put(`http://localhost:8080/api/cuentas/editarNombre/${codigo}`, nuevoNombre, {
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'text/plain' }
            });
            
            console.log('Response:', response); // Verifica la respuesta de la solicitud
            setMensaje(response.data);
            setNuevoNombre('');
        } catch (error) {
            console.error('Error al actualizar el nombre de la cuenta:', error);
            setError('Error al actualizar el nombre de la cuenta.');
        }
    };
    
    
    
    

    return (
        <div className="editar-cuenta">
            <h2>Editar Nombre de Cuenta</h2>
            <form onSubmit={handleSubmit}>
                <label htmlFor="cuenta-select">Seleccionar Cuenta:</label>
                <select
                    id="cuenta-select"
                    value={codigo}
                    onChange={(e) => {
                        setCodigo(e.target.value);
                        setNuevoNombre('');
                    }}
                    required
                >
                    <option value=""> Selecciona una cuenta </option>
                    {cuentas.map((cuenta) => (
                        <option key={cuenta.codigo} value={cuenta.codigo}>
                            {cuenta.nombre}
                        </option>
                    ))}
                </select>
                <input
                    type="text"
                    value={nuevoNombre}
                    onChange={(e) => setNuevoNombre(e.target.value)}
                    placeholder="Nuevo nombre de la cuenta"
                    required
                />
                <button type="submit">Actualizar Nombre</button>
            </form>
            {mensaje && <p className="mensaje-success">{mensaje}</p>}
            {error && <p className="mensaje-error">{error}</p>}
        </div>
    );
};

export default EditarCuenta;
