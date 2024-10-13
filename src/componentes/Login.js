import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async () => {
        if (!username || !password) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }

        setLoading(true); // Indicar que la solicitud está en curso

        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombreUsuario: username, contraseña: password }),
            });

            setLoading(false); // Terminar la carga

            if (!response.ok) {
                const errorData = await response.json();
                setMessage(`Error en el login: ${response.status} ${errorData.error || 'Error desconocido'}`);
                return;
            }

            const data = await response.json();
            localStorage.setItem('token', data.token); // Almacenar el token en localStorage
            setMessage('Inicio de sesión exitoso.');

            // Limpiar los campos
            setUsername('');
            setPassword('');

            // Redirigir al usuario a la página de cuentas o a donde desees
            navigate('/cuentas');
        } catch (error) {
            setLoading(false); // Terminar la carga
            setMessage(`Error de red: ${error.message}`);
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
