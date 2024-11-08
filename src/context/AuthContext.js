import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [role, setRole] = useState(''); // Cambio a un único rol
    const [userId, setUserId] = useState(null);
    useEffect(() => {
        logout();  // Opcional: cerrar sesión al refrescar

        const token = localStorage.getItem('token');
        const storedRole = localStorage.getItem('role');  // Cambiado a `role`
        const storedUserId = localStorage.getItem('userId');

        if (token) {
            setIsAuthenticated(true);
            if (storedRole) {
                setRole(storedRole);  // Establecer rol único
            }
            if (storedUserId) {
                setUserId(storedUserId);
            }
        }
    }, []);

    const login = (token, userRole, id) => {
        console.log("Roles recibidos en login:", userRole); // Log para verificar los roles
        localStorage.setItem('token', token);
        localStorage.setItem('role', userRole);  // Guardar rol como string
        localStorage.setItem('userId', id);
        setIsAuthenticated(true);
        setRole(userRole);  // Guardar rol único
        setUserId(id);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
        setIsAuthenticated(false);
        setRole('');  // Limpiar rol
        setUserId(null);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, role, userId, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
