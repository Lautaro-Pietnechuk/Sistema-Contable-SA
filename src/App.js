// src/App.js

import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Login from './componentes/Login'; // Asegúrate de importar tu componente de Login
import Register from './componentes/Register'; // Importa el componente de Registro
import Cuentas from './componentes/Cuentas'; // Importa el componente de Cuentas
import AgregarCuentas from './componentes/AgregarCuentas'; // Importa el componente para agregar cuentas
import ListaCuentas from './componentes/ListaCuentas'; // Importa el componente para listar cuentas
import Asientos from './componentes/Asientos'; // Importa el componente de Asientos
import AsignarRol from './componentes/AsignarRol'; // Importa el componente AsignarRol
import PrivateRoute from './componentes/PrivateRoute'; // Importa el componente PrivateRoute

const App = () => {
    return (
        <Router>
            <nav>
                <Link to="/login">Login</Link> &nbsp;
                <Link to="/register">Registro</Link> &nbsp;
                <Link to="/cuentas">Cuentas</Link> &nbsp;
                <Link to="/asientos">Asientos</Link> &nbsp;
                <Link to="/cuentas/agregar">Agregar Cuentas</Link> &nbsp; {/* Enlace a Agregar Cuentas */}
                <Link to="/asignar-rol">Asignar Rol</Link> {/* Enlace a Asignar Rol */}
            </nav>

            <Routes>
                <Route path="/login" element={<Login />} /> {/* Página de Login */}
                <Route path="/register" element={<Register />} /> {/* Página de Registro */}
                <Route path="/cuentas" element={
                    <PrivateRoute>
                        <Cuentas />
                    </PrivateRoute>
                } /> {/* Página de Cuentas */}
                <Route path="/cuentas/agregar" element={
                    <PrivateRoute>
                        <AgregarCuentas />
                    </PrivateRoute>
                } />  {/* Página para listar cuentas */}
                <Route path="/asientos" element={
                    <PrivateRoute>
                        <Asientos />
                    </PrivateRoute>
                } /> {/* Página de Asientos */}
                <Route path="/asignar-rol" element={
                    <PrivateRoute>
                        <AsignarRol />
                    </PrivateRoute>
                } /> {/* Página de Asignar Rol */}
            </Routes>
        </Router>
    );
};

export default App;
