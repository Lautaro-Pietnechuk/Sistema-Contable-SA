import React, { useContext, useEffect } from 'react';
import { Routes, Route, NavLink, Navigate, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import PrivateRoute from './PrivateRoute';
import Login from './Login';
import Register from './Register';
import Cuentas from './Cuentas';
import CrearCuenta from './CrearCuenta';
import EliminarCuenta from './EliminarCuenta';
import EditarCuenta from './EditarCuenta';
import Asientos from './Asientos';
import CrearAsiento from './CrearAsiento';
import EliminarUsuario from './EliminarUsuario';
import NotFound from './NotFound';
import LibroMayor from './LibroMayor';
import '../css/Nav.css';

const Nav = () => {
    const { isAuthenticated, role, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    // Verifica el rol al cargar el componente
    useEffect(() => {
        console.log("Rol desde AuthContext:", role); // Verifica el rol
        if (!isAuthenticated && window.location.pathname !== '/register') {
            navigate('/login');
        }
    }, [isAuthenticated, navigate, role]);

    return (
        <div style={{ display: 'flex' }}>
            <nav style={{ display: 'flex', flexDirection: 'column', padding: '10px', width: '200px' }}>
                {!isAuthenticated ? (
                    <>
                        <NavLink to="/login">Login</NavLink>
                        <NavLink to="/register">Registro</NavLink>
                    </>
                ) : (
                    <>
                        <NavLink to="/cuentas">Cuentas</NavLink>
                        <NavLink to="/asientos">Libro Diario</NavLink>
                        <NavLink to="/asientos/agregar">Agregar Asiento</NavLink>
                        {role[0] === 'ROLE_ADMINISTRADOR' && (
                            <>
                                <NavLink to="/cuentas/agregar">Agregar Cuentas</NavLink> {/* Mostrar solo para admin */}
                                <NavLink to="/cuentas/eliminar">Eliminar Cuentas</NavLink>
                                <NavLink to="/cuentas/editarNombre">Editar Cuentas</NavLink>
                                <NavLink to="/usuarios/eliminar">Eliminar Usuario</NavLink>
                            </>
                        )}
                        <NavLink to="/libro-mayor">Libro Mayor</NavLink>
                        <button onClick={logout} style={{ marginTop: '10px' }}>Cerrar Sesión</button>
                    </>
                )}
            </nav>

            <div style={{ flex: 1, padding: '20px' }}>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/cuentas" element={<PrivateRoute><Cuentas /></PrivateRoute>} />
                    <Route path="/cuentas/agregar" element={<PrivateRoute><CrearCuenta /></PrivateRoute>} />
                    <Route path="/cuentas/eliminar" element={<PrivateRoute><EliminarCuenta /></PrivateRoute>} />
                    <Route path="/cuentas/editarNombre" element={<PrivateRoute><EditarCuenta /></PrivateRoute>} />
                    <Route path="/asientos" element={<PrivateRoute><Asientos /></PrivateRoute>} />
                    <Route path="/asientos/agregar" element={<PrivateRoute><CrearAsiento /></PrivateRoute>} />
                    <Route path="/libro-mayor" element={<PrivateRoute><LibroMayor /></PrivateRoute>} />
                    <Route path="/usuarios/eliminar" element={<PrivateRoute><EliminarUsuario /></PrivateRoute>} />
                    <Route path="*" element={<NotFound />} />
                    <Route path="/" element={<Navigate to="/login" />} />
                </Routes>
            </div>
        </div>
    );
};

export default Nav;
