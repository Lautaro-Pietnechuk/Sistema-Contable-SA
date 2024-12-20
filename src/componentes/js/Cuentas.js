import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import '../css/Cuentas.css';

const Cuentas = () => {
    const [cuentas, setCuentas] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    // Definimos ordenarCuentas dentro de useCallback
    const ordenarCuentas = useCallback((cuentas) => {
        return cuentas
            .slice() // Crear una copia para evitar modificar el estado original
            .sort((a, b) => a.codigo - b.codigo) // Ordenar por código
            .map(cuenta => ({
                ...cuenta,
                subCuentas: cuenta.subCuentas ? ordenarCuentas(cuenta.subCuentas) : [] // Ordenar subcuentas recursivamente
            }));
    }, []); // Lista vacía para que la función no dependa de nada

    const obtenerCuentas = useCallback(async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            const respuesta = await axios.get('http://localhost:8080/api/cuentas/arbol', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            console.log('Respuesta de la API:', respuesta.data);
            const cuentasOrdenadas = ordenarCuentas(respuesta.data);
            setCuentas(cuentasOrdenadas);
            setError('');
        } catch (error) {
            console.error('Error obteniendo cuentas:', error);
            setError(error.response?.data?.message || 'Error al obtener cuentas.');
        } finally {
            setLoading(false);
        }
    }, [ordenarCuentas]); // `obtenerCuentas` depende de `ordenarCuentas`

    useEffect(() => {
        obtenerCuentas();
    }, [obtenerCuentas]); // Se ejecuta cuando `obtenerCuentas` cambia

    const CuentaItem = ({ cuenta }) => (
        <li>
            <div className="cuenta-item">
                <span>{cuenta.nombre}</span>
                <span>(Código: {cuenta.codigo})</span>
            </div>
            {cuenta.subCuentas && cuenta.subCuentas.length > 0 && (
                <ul>
                    {cuenta.subCuentas.map((subCuenta) => (
                        <CuentaItem key={subCuenta.codigo} cuenta={subCuenta} />
                    ))}
                </ul>
            )}
        </li>
    );

    return (
        <div className="container">
            <h2>Lista de Cuentas</h2>
            {loading ? (
                <p>Cargando...</p>
            ) : (
                <>
                    {error && <p style={{ color: 'red' }} aria-live="assertive">{error}</p>}
                    <ul>
                        {cuentas.length > 0 ? (
                            cuentas.map((cuenta) => (
                                <CuentaItem key={cuenta.codigo} cuenta={cuenta} />
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
