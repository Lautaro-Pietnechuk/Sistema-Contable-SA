package com.sa.contable.Configuracion;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private SecretKey secretKey; // Cambiar a SecretKey

    private final long EXPIRATION_TIME = 864_000_000; // 10 días en milisegundos


    public static void main(String[] args) {
        JwtUtil jwtUtil = new JwtUtil();
        jwtUtil.init(); // Asegúrate de inicializar la clave

        String token = jwtUtil.generarToken("usuario1", "ADMIN");
        System.out.println("Token generado: " + token);

        boolean isValid = jwtUtil.esTokenValido(token);
        System.out.println("¿Es válido el token? " + isValid);

        String usuario = jwtUtil.obtenerUsuarioDelToken(token);
        System.out.println("Usuario del token: " + usuario);

        String rol = jwtUtil.obtenerRolDelToken(token);
        System.out.println("Rol del token: " + rol);
    }

    @PostConstruct
    public void init() {
        // Genera una clave secreta segura
        String claveSecreta = "una_clave_secreta_larga_y_segura"; // Asegúrate de que tenga al menos 256 bits
        this.secretKey = Keys.hmacShaKeyFor(claveSecreta.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String usuario, String rol) {
        System.out.println("Clave secreta: " + secretKey);
        Claims claims = Jwts.claims().setSubject(usuario);
        claims.put("rol", rol);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey) // Utiliza la clave secreta generada
                .compact();
    }

    public String obtenerRolDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("rol", String.class);
    }
    
    public boolean esTokenValido(String token) {
        System.out.println("Clave secreta: " + secretKey);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("El token ha expirado: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("El token está mal formado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("El token no es soportado: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("La firma del token es inválida: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Argumento inválido: {}", e.getMessage());
            return false;
        }
    }
    

    public String obtenerUsuarioDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
