import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [roles, setRoles] = useState([]); // Nuevo estado para roles

    // Verificar si hay un token en localStorage al cargar la app
    useEffect(() => {
        const token = localStorage.getItem('token');
        const storedRoles = localStorage.getItem('roles'); // Obtener roles como cadena

        if (token) {
            setIsAuthenticated(true);
            if (storedRoles) {
                try {
                    const parsedRoles = JSON.parse(storedRoles); // Analizar roles solo si existe
                    setRoles(parsedRoles); // Configurar roles si existen
                } catch (error) {
                    console.error("Error parsing roles:", error);
                    setRoles([]); // O manejar el error según lo necesites
                }
            }
        }
    }, []);

    // Función para manejar el login
    const login = (token, userRoles) => {
        localStorage.setItem('token', token);
        localStorage.setItem('roles', JSON.stringify(userRoles)); // Almacenar roles en localStorage
        setIsAuthenticated(true);
        setRoles(userRoles); // Configurar roles en el estado
    };

    // Función para manejar el logout
    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('roles'); // Eliminar roles de localStorage
        setIsAuthenticated(false);
        setRoles([]); // Limpiar roles en el estado
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, roles, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
