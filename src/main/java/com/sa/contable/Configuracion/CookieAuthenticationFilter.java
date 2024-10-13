/* package com.sa.contable.Configuracion;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = extraerTokenDeCookie(request);
        if (token != null) {
            return token; // Retorna el token JWT
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return ""; // No se requieren credenciales adicionales
    }

    private String extraerTokenDeCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (getPreAuthenticatedPrincipal(request) != null) {
            // Configurar el contexto de seguridad con el token
            SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("user", ""));
        }
        chain.doFilter(request, response); // Asegúrate de que este método esté correctamente invocado
    }
}
 */