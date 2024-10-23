import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const Cuentas = () => {
    const [cuentas, setCuentas] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    // Función para estructurar las cuentas en árbol
    const construirArbolDeCuentas = (cuentasPlanas) => {
        const cuentasMap = {};

        // Inicializa el mapa de cuentas por código
        cuentasPlanas.forEach(cuenta => {
            cuentasMap[cuenta.codigo] = { ...cuenta, subCuentas: [] };
        });

        const arbol = [];

        cuentasPlanas.forEach(cuenta => {
            const codigo = String(cuenta.codigo); // Convertir a cadena

            const codigoPadre = codigo.length === 3 && codigo.startsWith('110') 
                ? '100' 
                : codigo.startsWith('11') && codigo.length === 3 
                ? '110' 
                : null;

            if (codigoPadre && cuentasMap[codigoPadre]) {
                cuentasMap[codigoPadre].subCuentas.push(cuentasMap[codigo]);
            } else {
                arbol.push(cuentasMap[codigo]);
            }
        });

        return arbol;
    };

    // Función para obtener cuentas usando useCallback
    const obtenerCuentas = useCallback(async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            console.log('Token recuperado:', token);
            const respuesta = await axios.get('http://localhost:8080/api/cuentas', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            const cuentasEnArbol = construirArbolDeCuentas(respuesta.data);
            setCuentas(cuentasEnArbol);
            setError('');
        } catch (error) {
            console.error('Error obteniendo cuentas:', error);
            setCuentas([]);
            setError(error.response?.data?.message || 'Error al obtener cuentas.');
        } finally {
            setLoading(false);
        }
    }, []); // Puedes agregar 'token' aquí si se obtiene de otra fuente y puede cambiar.

    useEffect(() => {
        obtenerCuentas();
    }, [obtenerCuentas]);

    // Componente recursivo para renderizar el árbol de cuentas
    const CuentaItem = ({ cuenta }) => (
        <li>
            {cuenta.nombre} ({cuenta.codigo})
            {cuenta.subCuentas.length > 0 && (
                <ul>
                    {cuenta.subCuentas.map(subCuenta => (
                        <CuentaItem key={subCuenta.codigo} cuenta={subCuenta} />
                    ))}
                </ul>
            )}
        </li>
    );

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
