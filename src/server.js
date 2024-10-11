const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken'); // Para manejar tokens JWT
const app = express();

// Configura CORS para permitir solicitudes desde http://localhost:3000
app.use(cors({
    origin: 'http://localhost:3000',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

// Middleware para parsear JSON
app.use(express.json());

// Middleware para verificar el token JWT y el rol de Administrador
const verificarRolAdmin = (req, res, next) => {
    const token = req.headers['authorization'];
    if (!token) {
        return res.status(403).json({ message: 'Acceso denegado. Se requiere autenticación.' });
    }

    try {
        const decoded = jwt.verify(token, 'tu_secreto_jwt'); // Verifica el token JWT
        if (decoded.rol === 'Administrador') {
            next(); // El usuario tiene el rol de Administrador, continuar con la solicitud
        } else {
            res.status(403).json({ message: 'Acceso denegado. Se requiere rol de Administrador.' });
        }
    } catch (error) {
        res.status(403).json({ message: 'Token inválido.' });
    }
};

// Ruta para manejar la solicitud POST
app.post('/api/cuentas', verificarRolAdmin, (req, res) => {
    // Lógica para manejar la solicitud POST
    res.json({ message: 'Cuenta agregada exitosamente!' });
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Servidor corriendo en el puerto ${PORT}`);
});