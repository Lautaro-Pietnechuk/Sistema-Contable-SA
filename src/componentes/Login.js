import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Importar useNavigate para redirigir

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate(); // Crear instancia de useNavigate

    const handleLogin = async () => {
        if (!username || !password) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombreUsuario: username, contraseña: password }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                setMessage(`Error en el login: ${response.status} ${errorData.error}`);
                return;
            }

            const data = await response.json();
            localStorage.setItem('token', data.token); // Almacenar el token en localStorage
            setMessage('Inicio de sesión exitoso.');

            // Redirigir al usuario a la página de cuentas o a donde desees
            navigate('/cuentas');
        } catch (error) {
            setMessage(`Error de red: ${error.message}`);
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <input
                type="text"
                placeholder="Nombre de usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <input
                type="password"
                placeholder="Contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            <button onClick={handleLogin}>Login</button>
            {message && <p>{message}</p>}
        </div>
    );
};

export default Login;
