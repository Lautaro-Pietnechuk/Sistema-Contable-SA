package com.sa.contable.Configuracion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    private SecretKey secretKey; // Cambiar a SecretKey

    private final long EXPIRATION_TIME = 864_000_000; // 10 días en milisegundos

    @PostConstruct
    public void init() {
        // Genera una clave secreta segura
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generarToken(String usuario, String rol) {
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

    public boolean esTokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Utiliza la misma clave secreta para verificar
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
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
