import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../AuthContext'; // Importar el contexto de autenticación

const PrivateRoute = ({ children }) => {
    const { isAuthenticated } = useContext(AuthContext); // Obtener el estado de autenticación del contexto

    return isAuthenticated ? children : <Navigate to="/login" />; // Redirigir si no está autenticado
};

export default PrivateRoute;
