import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [roles, setRoles] = useState([]);
    const [userId, setUserId] = useState(null);

    // Verificar el estado de autenticación al cargar la app
    useEffect(() => {
        const token = localStorage.getItem('token');
        const storedRoles = localStorage.getItem('roles');
        const storedUserId = localStorage.getItem('userId');

        if (token) {
            setIsAuthenticated(true);
            if (storedRoles) {
                try {
                    const parsedRoles = JSON.parse(storedRoles);
                    setRoles(parsedRoles);
                } catch (error) {
                    console.error("Error parsing roles:", error);
                    setRoles([]);
                }
            }
            if (storedUserId) {
                setUserId(storedUserId);
            }
        }
    }, []);

    // Función para manejar el login
    const login = (token, userRoles, id) => {
        localStorage.setItem('token', token);
        localStorage.setItem('roles', JSON.stringify(userRoles));
        localStorage.setItem('userId', id);
        setIsAuthenticated(true);
        setRoles(userRoles);
        setUserId(id);
    };

    // Función para manejar el logout
    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('roles');
        localStorage.removeItem('userId');
        setIsAuthenticated(false);
        setRoles([]);
        setUserId(null);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, roles, userId, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
