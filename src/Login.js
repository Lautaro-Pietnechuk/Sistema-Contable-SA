import React, { useState } from 'react';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

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
                const errorText = await response.text();
                setMessage(`Error en el login: ${response.status} ${errorText}`);
                return;
            }

            const data = await response.text(); 
            setMessage(data);
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
