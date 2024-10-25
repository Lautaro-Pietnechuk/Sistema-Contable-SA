package com.sa.contable.configuracion;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Leer el valor de jwt.secret desde application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;  // Declarar aquí la clave secreta

    private final long EXPIRATION_TIME = 864_000_000; // 10 días en milisegundos

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET no es válida o no está definida.");
        }
        // Convertir el secret a una clave HMAC
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        logger.info("JwtUtil inicializado correctamente.");
    }

    // Generar un nuevo token JWT
    public String generarToken(String usuario, String rol, String id) {
        logger.debug("Generando token para el usuario: {}", usuario);
        Claims claims = Jwts.claims().setSubject(usuario);
        claims.put("rol", rol);
        claims.put("id", id);  // Añade la ID del usuario aquí
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
    
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)  // Utiliza la clave secreta generada
                .compact();
    
        logger.info("Token generado exitosamente para el usuario: {}", usuario);
        return token;
    }
    

    // Obtener el rol del usuario a partir del token
    public String obtenerRolDelToken(String token) {
        logger.debug("Obteniendo rol del token.");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String rol = claims.get("rol", String.class);
        logger.info("Rol obtenido del token: {}", rol);
        return rol;
    }

    public String obtenerIdDelToken(String token) {
        logger.debug("Obteniendo ID del token.");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    
        String id = claims.get("id", String.class);  // Obtener la ID del claims
        logger.info("ID obtenida del token: {}", id);
        return id;
    }
    
    // Validar si el token es válido
    public boolean esTokenValido(String token) {
        logger.debug("Validando token.");
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            logger.info("El token es válido.");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("El token ha expirado: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("Token inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al validar el token: {}", e.getMessage());
        }
        return false; // Devolviendo false en caso de excepción
    }

    // Obtener el nombre de usuario a partir del token
    public String obtenerUsuarioDelToken(String token) {
        logger.debug("Obteniendo usuario del token.");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String usuario = claims.getSubject();
        logger.info("Usuario obtenido del token: {}", usuario);
        return usuario;
    }

    // Obtener los claims del token
    private Claims obtenerClaims(String token) {
        logger.debug("Obteniendo claims del token.");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error al obtener claims del token: {}", e.getMessage());
            return null;
        }
    }
}
