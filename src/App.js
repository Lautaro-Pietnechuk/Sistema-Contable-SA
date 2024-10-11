// src/App.js

import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Login from './componentes/Login'; // Asegúrate de importar tu componente de Login
import Register from './componentes/Register'; // Importa el componente de Registro
import Cuentas from './componentes/Cuentas'; // Importa el componente de Cuentas
import AgregarCuentas from './componentes/AgregarCuentas'; // Importa el componente para agregar cuentas
import ListaCuentas from './componentes/ListaCuentas'; // Importa el componente para listar cuentas
import Asientos from './componentes/Asientos'; // Importa el componente de Asientos
import AsignarRol from './componentes/AsignarRol'; // Importa el componente AsignarRol

const App = () => {
    return (
        <Router>
            <nav>
                <Link to="/">Login</Link> &nbsp;
                <Link to="/register">Registro</Link> &nbsp;
                <Link to="/cuentas">Cuentas</Link> &nbsp;
                <Link to="/asientos">Asientos</Link> &nbsp;
                <Link to="/cuentas/agregar">Agregar Cuentas</Link> &nbsp; {/* Enlace a Agregar Cuentas */}
                <Link to="/cuentas/lista">Lista de Cuentas</Link> &nbsp; {/* Enlace a Lista de Cuentas */}
                <Link to="/asignar-rol">Asignar Rol</Link> {/* Enlace a Asignar Rol */}
            </nav>

            <Routes>
                <Route path="/" element={<Login />} /> {/* Página de Login */}
                <Route path="/register" element={<Register />} /> {/* Página de Registro */}
                <Route path="/cuentas" element={<Cuentas />} /> {/* Página de Cuentas */}
                <Route path="/cuentas/agregar" element={<AgregarCuentas />} /> {/* Página para agregar cuentas */}
                <Route path="/cuentas/lista" element={<ListaCuentas />} /> {/* Página para listar cuentas */}
                <Route path="/asientos" element={<Asientos />} /> {/* Página de Asientos */}
                <Route path="/asignar-rol" element={<AsignarRol />} /> {/* Página de Asignar Rol */}
            </Routes>
        </Router>
    );
};

export default App;