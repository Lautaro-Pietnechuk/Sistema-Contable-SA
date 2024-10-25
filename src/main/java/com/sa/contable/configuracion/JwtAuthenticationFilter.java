package com.sa.contable.configuracion;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // LOG: Solicitud entrante
        System.out.println("Procesando solicitud para: " + request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("Token JWT detectado: " + jwt);
            username = jwtUtil.obtenerUsuarioDelToken(jwt);
            System.out.println("Usuario extraído del token: " + username);
        } else {
            System.out.println("No se encontró encabezado Authorization o no tiene formato válido.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Autenticando al usuario: " + username);

            if (jwtUtil.esTokenValido(jwt)) {
                System.out.println("Token válido para el usuario: " + username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority(jwtUtil.obtenerRolDelToken(jwt)))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("Token inválido para el usuario: " + username);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("El usuario ya está autenticado: " + username);
        }

        filterChain.doFilter(request, response);
    }
}
