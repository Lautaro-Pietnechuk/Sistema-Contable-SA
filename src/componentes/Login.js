import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { AuthContext } from '../AuthContext';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login } = useContext(AuthContext); // Usar el contexto de autenticación

    const handleLogin = async () => {
        if (!username || !password) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }
    
        setLoading(true); // Mostrar indicador de carga
    
        try {
            const response = await axios.post('http://localhost:8080/api/login', {
                nombreUsuario: username,
                contraseña: password,
            });
    
            // Asegúrate de que el servidor esté devolviendo un solo rol
            console.log("Respuesta del servidor:", response.data);
            const { token, rol } = response.data; // Extraer token y rol

            login(token, rol); // Llamar a la función login del contexto con token y rol
    
            setMessage('Inicio de sesión exitoso.');
            navigate('/cuentas'); // Redirigir al usuario
        } catch (error) {
            setLoading(false);
            if (error.response) {
                setMessage(`Error en el login: ${error.response.status} ${error.response.data.error || 'Error desconocido'}`);
            } else {
                setMessage(`Error de red: ${error.message}`);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto' }}>
            <h2>Iniciar Sesión</h2>
            <input
                type="text"
                placeholder="Nombre de usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{ width: '100%', padding: '10px', margin: '5px 0' }}
            />
            <input
                type="password"
                placeholder="Contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: '100%', padding: '10px', margin: '5px 0' }}
            />
            <button onClick={handleLogin} disabled={loading} style={{ width: '100%', padding: '10px', margin: '5px 0' }}>
                {loading ? 'Cargando...' : 'Iniciar Sesión'}
            </button>
            {message && <p style={{ color: 'red' }}>{message}</p>}
        </div>
    );
};

export default Login;
