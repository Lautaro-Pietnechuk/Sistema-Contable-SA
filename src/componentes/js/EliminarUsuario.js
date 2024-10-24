import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const EliminarUsuario = () => {
    const [usuarios, setUsuarios] = useState([]);
    const [usuarioSeleccionado, setUsuarioSeleccionado] = useState('');
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const [token, setToken] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
            obtenerUsuarios(storedToken);
        }
    }, [navigate]);

    const obtenerUsuarios = async (token) => {
        try {
            const respuesta = await axios.get('http://localhost:8080/api/usuarios', {
                headers: { Authorization: `Bearer ${token}` },
            });
            setUsuarios(respuesta.data);
        } catch (error) {
            console.error('Error al obtener usuarios:', error);
            setMensajeError('Error al obtener usuarios.');
        }
    };

    const manejarEnvio = async (e) => {
        e.preventDefault();
        if (!usuarioSeleccionado) {
            setMensajeError('Seleccione un usuario para eliminar.');
            return;
        }
        try {
            await axios.delete(`http://localhost:8080/api/usuarios/${usuarioSeleccionado}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setMensajeExito('Usuario eliminado con éxito.');
            setMensajeError('');
            setUsuarioSeleccionado('');
            obtenerUsuarios(token); // Actualizar la lista de usuarios
            setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
        } catch (error) {
            if (error.response) {
                console.error('Error del servidor:', error.response.data);
                if (error.response.status === 403) {
                    setMensajeError('Necesitas permisos de administrador para poder eliminar un usuario.');
                } else {
                    setMensajeError(`Error: ${error.response.data.error || error.response.data.message}`);
                }
            } else {
                console.error('Error al eliminar el usuario:', error.message);
                setMensajeError('Error desconocido al eliminar el usuario.');
            }
        }
    };

    return (
        <div>
            <h2>Eliminar Usuario</h2>
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            {mensajeError && <p className="mensaje-error">{mensajeError}</p>}
            <form onSubmit={manejarEnvio}>
                <div>
                    <label htmlFor="usuario">Seleccionar usuario a eliminar:</label>
                    <select
                        id="usuario"
                        value={usuarioSeleccionado}
                        onChange={(e) => setUsuarioSeleccionado(e.target.value)}
                        required
                    >
                        <option value="">Seleccionar usuario</option>
                        {usuarios.map((usuario) => (
                            <option key={usuario.id} value={usuario.id}>
                                {usuario.nombre} - {usuario.email}
                            </option>
                        ))}
                    </select>
                </div>
                <button type="submit">Eliminar Usuario</button>
            </form>
        </div>
    );
};

export default EliminarUsuario;
