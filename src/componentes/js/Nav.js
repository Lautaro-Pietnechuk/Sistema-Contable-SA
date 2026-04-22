import React, { useContext, useEffect, useState } from 'react';
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
import RegistrarVenta from './RegistrarVenta';
import ListarVentas from './ListarVentas';
import RegistrarCliente from './RegistrarCliente';
import ListarClientes from './ListarClientes';
import CrearProducto from './CrearProducto';    
import ListarProductos from './ListarProductos';
import '../css/Nav.css';

// Componente auxiliar para los menús desplegables (Acordeón)
const NavSection = ({ title, isOpen, toggle, children }) => (
    <div style={{ marginBottom: '10px' }}>
        <button 
            onClick={toggle} 
            style={{ 
                width: '100%', textAlign: 'left', padding: '8px', 
                background: 'transparent', border: '1px solid #ddd', 
                cursor: 'pointer', fontWeight: 'bold', 
                display: 'flex', justifyContent: 'space-between',
                borderRadius: '4px'
            }}
        >
            {title} <span>{isOpen ? '▾' : '▸'}</span>
        </button>
        {isOpen && (
            <div style={{ marginLeft: '15px', display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
                {children}
            </div>
        )}
    </div>
);

const Nav = () => {
    const { isAuthenticated, role, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    // Estados independientes para abrir/cerrar cada sección
    const [openCuentas, setOpenCuentas] = useState(false);
    const [openAsientos, setOpenAsientos] = useState(false);
    const [openVentas, setOpenVentas] = useState(false);
    const [openClientes, setOpenClientes] = useState(false);
    const [openProductos, setOpenProductos] = useState(false);
    const [openConfig, setOpenConfig] = useState(false);

    const isAdmin = role && role[0] === 'ROLE_ADMINISTRADOR';

    useEffect(() => {
        if (!isAuthenticated && window.location.pathname !== '/register') {
            navigate('/login');
        }
    }, [isAuthenticated, navigate, role]);

    return (
        <div style={{ display: 'flex' }}>
            <nav style={{ display: 'flex', flexDirection: 'column', padding: '10px', width: '220px', minHeight: '100vh', borderRight: '1px solid #ccc', overflowY: 'auto' }}>
                {!isAuthenticated ? (
                    <>
                        <NavLink to="/login">Login</NavLink>
                        <NavLink to="/register">Registro</NavLink>
                    </>
                ) : (
                    <>
                        {/* Sección Cuentas */}
                        <NavSection title="Cuentas" isOpen={openCuentas} toggle={() => setOpenCuentas(!openCuentas)}>
                            <NavLink to="/cuentas">Cuentas</NavLink>
                            {isAdmin && (
                                <>
                                    <NavLink to="/cuentas/agregar">Agregar Cuentas</NavLink>
                                    <NavLink to="/cuentas/eliminar">Eliminar Cuentas</NavLink>
                                    <NavLink to="/cuentas/editarNombre">Editar Cuentas</NavLink>
                                </>
                            )}
                        </NavSection>

                        {/* Sección Asientos */}
                        <NavSection title="Asientos" isOpen={openAsientos} toggle={() => setOpenAsientos(!openAsientos)}>
                            <NavLink to="/asientos/agregar">Agregar Asiento</NavLink>
                            <NavLink to="/asientos">Libro Diario</NavLink>
                            <NavLink to="/libro-mayor">Libro Mayor</NavLink>
                        </NavSection>

                        {/* Sección Ventas */}
                        <NavSection title="Ventas" isOpen={openVentas} toggle={() => setOpenVentas(!openVentas)}>
                            <NavLink to="/registrar-venta">Registrar Venta</NavLink>
                            <NavLink to="/ventas">Listar Ventas</NavLink>
                        </NavSection>

                        {/* Sección Clientes */}
                        <NavSection title="Clientes" isOpen={openClientes} toggle={() => setOpenClientes(!openClientes)}>
                            <NavLink to="/registrar-cliente">Registrar Cliente</NavLink>
                            <NavLink to="/clientes">Listar Clientes</NavLink>
                        </NavSection>

                        {/* Sección Productos */}
                        <NavSection title="Productos" isOpen={openProductos} toggle={() => setOpenProductos(!openProductos)}>
                            <NavLink to="/productos/crear">Crear Producto</NavLink>
                            <NavLink to="/productos">Listar Productos</NavLink>
                        </NavSection>

                        {/* Sección Configuración (Solo visible si hay opciones para el usuario) */}
                        {isAdmin && (
                            <NavSection title="Configuración" isOpen={openConfig} toggle={() => setOpenConfig(!openConfig)}>
                                <NavLink to="/usuarios/eliminar">Eliminar Usuario</NavLink>
                            </NavSection>
                        )}

                        <button onClick={logout} style={{ marginTop: '20px', padding: '8px', cursor: 'pointer' }}>
                            Cerrar Sesión
                        </button>
                    </>
                )}
            </nav>

            {/* Rutas */}
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
                    <Route path="/registrar-venta" element={<PrivateRoute><RegistrarVenta /></PrivateRoute>} />
                    <Route path="/ventas" element={<PrivateRoute><ListarVentas show={true} /></PrivateRoute>} />
                    <Route path="/registrar-cliente" element={<PrivateRoute><RegistrarCliente /></PrivateRoute>} />
                    <Route
                        path="/clientes"
                        element={
                            <PrivateRoute>
                                <ListarClientes show={true} handleClose={() => navigate('/registrar-cliente')} />
                            </PrivateRoute>
                        }
                    />
                    <Route path="/productos/crear" element={<PrivateRoute><CrearProducto /></PrivateRoute>} />
                    <Route path="/productos" element={<PrivateRoute><ListarProductos show={true} /></PrivateRoute>} />
                    <Route path="*" element={<NotFound />} />
                    <Route path="/" element={<Navigate to="/login" />} />
                </Routes>
            </div>
        </div>
    );
};

export default Nav;