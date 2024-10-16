// axiosConfig.js
import axios from 'axios';

// Configurar un interceptor para agregar el token a las solicitudes
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token'); // Recuperar el token
    if (token) {
        config.headers.Authorization = `Bearer ${token}`; // Agregar el token al encabezado
    }
    return config;
}, error => {
    return Promise.reject(error);
});

export default axios;
