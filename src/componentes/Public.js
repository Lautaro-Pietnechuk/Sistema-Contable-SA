import axios from 'axios';

// Función para probar el endpoint público
async function testPublicEndpoint() {
    try {
        const response = await axios.get('http://localhost:8080/api/public/test'); // Asegúrate de usar el puerto correcto
        console.log('Respuesta del servidor:', response.data);
    } catch (error) {
        console.error('Error al acceder al endpoint público:', error);
    }
}

// Llama a la función
testPublicEndpoint();
