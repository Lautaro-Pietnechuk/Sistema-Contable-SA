/* package com.sa.contable.Configuracion;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTInterceptor implements HandlerInterceptor {

    private final String SECRET_KEY = "tu_secreto_jwt";  // La misma clave que usaste al generar el token

    @Override
    public boolean preHandle(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        try {
            token = token.substring(7);  // Quitar "Bearer "
            @SuppressWarnings("deprecation")
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String rol = claims.get("rol", String.class);
            if (!"Administrador".equals(rol)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            return true;
        } catch (@SuppressWarnings("deprecation") io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.SignatureException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }
}
 */