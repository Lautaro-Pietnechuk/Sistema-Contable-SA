import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Login from './Login'; // Asegúrate de importar tu componente de Login
import Register from './Register'; // Importa el componente de Registro
import Cuentas from './Cuentas'; // Importa el componente de Cuentas
import Asientos from './Asientos'; // Importa el componente de Asientos

const App = () => {
    return (
        <Router>
            <nav>
                <Link to="/">Login</Link> &nbsp;
                <Link to="/register">Registro</Link> &nbsp;
                <Link to="/cuentas">Cuentas</Link> &nbsp;
                <Link to="/asientos">Asientos</Link>
            </nav>

            <Routes>
                <Route path="/" element={<Login />} /> {/* Página de Login */}
                <Route path="/register" element={<Register />} /> {/* Página de Registro */}
                <Route path="/cuentas" element={<Cuentas />} /> {/* Página de Cuentas */}
                <Route path="/asientos" element={<Asientos />} /> {/* Página de Asientos */}
            </Routes>
        </Router>
    );
};

export default App;
