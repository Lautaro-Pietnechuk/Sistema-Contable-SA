import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Cuentas = () => {
    const [cuentas, setCuentas] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    const obtenerCuentas = async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            console.log('Token:', token);
            const respuesta = await axios.get('http://localhost:8080/api/cuentas', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            console.log('Respuesta:', respuesta.data);
            
            setCuentas(respuesta.data);
            setError('');
        } catch (error) {
            console.error('Error obteniendo cuentas:', error);
            setCuentas([]);
            setError(error.response?.data?.message || 'Error al obtener cuentas.'); // Mensaje de error detallado
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        obtenerCuentas();
    }, []);

    return (
        <div>
            <h2>Lista de Cuentas</h2>
            {loading ? (
                <p>Cargando...</p>
            ) : (
                <>
                    {error && <p style={{ color: 'red' }} aria-live="assertive">{error}</p>}
                    <ul>
                        {Array.isArray(cuentas) && cuentas.length > 0 ? (
                            cuentas.map((cuenta) => (
                                <li key={cuenta.id}>{cuenta.nombre}</li>
                            ))
                        ) : (
                            <li>No hay cuentas disponibles</li>
                        )}
                    </ul>
                </>
            )}
        </div>
    );
};

export default Cuentas;
