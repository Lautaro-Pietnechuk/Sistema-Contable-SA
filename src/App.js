import { BrowserRouter as Router, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext, AuthProvider } from './context/AuthContext';
import Login from './componentes/js/Login';
import Register from './componentes/js/Register';
import Cuentas from './componentes/js/Cuentas';
import CrearCuenta from './componentes/js/CrearCuenta';
import EliminarCuenta from './componentes/js/EliminarCuenta'; // Importar el componente EliminarCuenta
import EditarCuenta from './componentes/js/EditarCuenta'; // Importar el componente EditarCuenta
import Asientos from './componentes/js/Asientos';
import CrearAsiento from './componentes/js/CrearAsiento'; // Importar el componente AgregarAsiento
import AsignarRol from './componentes/js/AsignarRol';
import PrivateRoute from './componentes/js/PrivateRoute';
import NotFound from './componentes/js/NotFound'; // Corregido: eliminado 's'

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
                            <CrearCuenta />
                        </PrivateRoute>
                    } />
                    <Route path="/cuentas/eliminar" element={ // Ruta para Eliminar Cuenta
                        <PrivateRoute>
                            <EliminarCuenta />
                        </PrivateRoute>
                    } />
                    <Route path="/cuentas/editarNombre" element={ // Ruta para Editar Cuenta
                        <PrivateRoute>
                            <EditarCuenta />
                        </PrivateRoute>
                    } />
                    <Route path="/asientos" element={
                        <PrivateRoute>
                            <Asientos />
                        </PrivateRoute>
                    } />
                    <Route path="/asientos/agregar" element={ // Ruta para Agregar Asiento
                        <PrivateRoute>
                            <CrearAsiento />
                        </PrivateRoute>
                    } />
                    <Route path="/asignar-rol" element={
                        <PrivateRoute>
                            <AsignarRol />
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
                        <NavLink to="/asientos/agregar">Agregar Asiento</NavLink> &nbsp; {/* Enlace para Agregar Asiento */}
                        <NavLink to="/cuentas/agregar">Agregar Cuentas</NavLink> &nbsp;
                        <NavLink to="/cuentas/eliminar">Eliminar Cuentas</NavLink> &nbsp; {/* Enlace para Eliminar Cuenta */}
                        <NavLink to="/cuentas/editarNombre">Editar Cuentas</NavLink> &nbsp; {/* Enlace para Editar Cuenta */}
                        <NavLink to="/asignar-rol">Asignar Rol</NavLink> &nbsp;
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
