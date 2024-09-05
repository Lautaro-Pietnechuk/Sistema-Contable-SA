const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { Pool } = require('pg');


const app = express();
app.use(bodyParser.json());
app.use(cors());
app.use(express.json());

const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'login_db', //CAMBIAR** PONER NOMBRE DE LA BD
  password: '****', //CAMBIAR** PONER SU CONTRASEÑA
  port: 5432,
});

app.post('/login', async (req, res) => {
  const { username, password } = req.body;
  const result = await pool.query('SELECT * FROM users WHERE username = $1 AND password = $2', [username, password]);
  if (result.rows.length > 0) {
    res.send({ message: 'Login exitoso' });
  } else {
    res.status(401).send({ message: 'Usuario o contraseña incorrectos' });
  }
});

app.listen(3001, () => {
  console.log('Servidor corriendo en el puerto 3001');
});

app.post('/register', async (req, res) => {
  const { username, password } = req.body;
  console.log('Datos recibidos:', username, password); // Log de depuración
  try {
    const result = await pool.query('INSERT INTO users (username, password) VALUES ($1, $2) RETURNING *', [username, password]);
    res.send({ message: 'Usuario registrado exitosamente' });
  } catch (error) {
    console.error('Error en la inserción:', error); // Log de depuración
    if (error.code === '23505') {
      res.status(400).send({ message: 'El usuario ya existe' });
    } else {
      res.status(500).send({ message: 'Error en el servidor' });
    }
  }
});
