import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import Nav from './componentes/js/Nav';
import { AuthProvider } from './context/AuthContext';

const App = () => {
    return (
        <AuthProvider>
            <Router>
                <Nav />
            </Router>
        </AuthProvider>
    );
};

export default App;