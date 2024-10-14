import { BrowserRouter as Router, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext, AuthProvider } from './AuthContext'; // Importar el contexto de autenticación
import Login from './componentes/Login';
import Register from './componentes/Register';
import Cuentas from './componentes/Cuentas';
import AgregarCuentas from './componentes/AgregarCuentas';
import EliminarCuenta from './componentes/EliminarCuenta'; // Importar el componente EliminarCuenta
import Asientos from './componentes/Asientos';
import AsignarRol from './componentes/AsignarRol';
import Roles from './componentes/Roles';
import PrivateRoute from './componentes/PrivateRoute';
import NotFound from './componentes/NotFound';

const App = () => {
    return (
        <AuthProvider>
            <Router>
                <Navigation />
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/cuentas" element={
                        <PrivateRoute>
                            <Cuentas />
                        </PrivateRoute>
                    } />
                    <Route path="/cuentas/agregar" element={
                        <PrivateRoute>
                            <AgregarCuentas />
                        </PrivateRoute>
                    } />
                    <Route path="/cuentas/eliminar" element={ // Ruta para Eliminar Cuenta
                        <PrivateRoute>
                            <EliminarCuenta />
                        </PrivateRoute>
                    } />
                    <Route path="/asientos" element={
                        <PrivateRoute>
                            <Asientos />
                        </PrivateRoute>
                    } />
                    <Route path="/asignar-rol" element={
                        <PrivateRoute>
                            <AsignarRol />
                        </PrivateRoute>
                    } />
                    <Route path="/roles" element={
                        <PrivateRoute>
                            <Roles />
                        </PrivateRoute>
                    } />
                    <Route path="*" element={<NotFound />} />
                    <Route path="/" element={<Navigate to="/login" />} /> {/* Redirigir al login si no está autenticado */}
                </Routes>
            </Router>
        </AuthProvider>
    );
};

const Navigation = () => {
    const { isAuthenticated, logout } = useContext(AuthContext);

    return (
        <nav style={{ display: 'flex', justifyContent: 'space-between', padding: '10px' }}>
            <div>
                {!isAuthenticated ? (
                    <>
                        <NavLink to="/login">Login</NavLink> &nbsp;
                        <NavLink to="/register">Registro</NavLink> &nbsp;
                    </>
                ) : (
                    <>
                        <NavLink to="/cuentas">Cuentas</NavLink> &nbsp;
                        <NavLink to="/asientos">Asientos</NavLink> &nbsp;
                        <NavLink to="/cuentas/agregar">Agregar Cuentas</NavLink> &nbsp;
                        <NavLink to="/cuentas/eliminar">Eliminar Cuentas</NavLink> &nbsp; {/* Enlace para Eliminar Cuenta */}
                        <NavLink to="/asignar-rol">Asignar Rol</NavLink> &nbsp;
                        <NavLink to="/roles">Roles</NavLink> &nbsp;
                    </>
                )}
            </div>

            {isAuthenticated && (
                <div>
                    <button onClick={logout} style={{ marginLeft: 'auto' }}>Cerrar Sesión</button>
                </div>
            )}
        </nav>
    );
};

export default App;
