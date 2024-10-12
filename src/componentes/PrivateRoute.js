// src/componentes/PrivateRoute.js

import { Navigate } from 'react-router-dom';

const PrivateRoute = ({ children }) => {
    const isAuthenticated = !!localStorage.getItem('token'); // Verifica si hay un token

    return isAuthenticated ? children : <Navigate to="/login" />;
};

export default PrivateRoute;
