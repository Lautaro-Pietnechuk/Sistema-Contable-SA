// src/componentes/AgregarCuentas.js

import React, { useState } from 'react';
import axios from 'axios';
import './AgregarCuentas.css'; // Importa el archivo CSS

const AgregarCuentas = () => {
    const [nombre, setNombre] = useState('');
    const [tipo, setTipo] = useState('');
    const [recibeSaldo, setRecibeSaldo] = useState(false);
    const [codigo, setCodigo] = useState('');

    const manejarEnvio = async (e) => {
        e.preventDefault(); // Prevenir el comportamiento por defecto del formulario

        const nuevaCuenta = {
            nombre,
            tipo,
            codigo,
            recibeSaldo
        };

        try {
            await axios.post('/api/cuentas', nuevaCuenta, {
                headers: {
                    'x-rol': 'administrador' // Suponiendo que el usuario tiene el rol de administrador
                }
            }); // Usa la ruta relativa
            // Limpiar los campos después de agregar la cuenta
            setNombre('');
            setTipo('');
            setCodigo('');
            setRecibeSaldo(false);
            alert('Cuenta agregada exitosamente!');
        } catch (error) {
            console.error('Error agregando la cuenta:', error);
            alert('Error al agregar la cuenta. Intenta nuevamente.');
        }
    };

    const handleTipoChange = (e) => {
        const selectedTipo = e.target.value;
        setTipo(selectedTipo);
        if (selectedTipo === 'Activo') {
            setCodigo(100);
        } else if (selectedTipo === 'Pasivo') {
            setCodigo(200);
        }
    };

    const handleCodigoChange = (e) => {
        const newCodigo = e.target.value;
        setCodigo(newCodigo);

        if (newCodigo.length >= 3 && newCodigo.slice(-1) !== '0') {
            setRecibeSaldo(true);
        } else {
            setRecibeSaldo(false);
        }
    };

    return (
        <div>
            <h2>Agregar Nueva Cuenta</h2>
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
                    <select
                        id="tipo"
                        value={tipo}
                        onChange={handleTipoChange}
                        required
                    >
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