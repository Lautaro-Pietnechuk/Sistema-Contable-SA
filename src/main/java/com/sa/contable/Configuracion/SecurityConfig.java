package com.sa.contable.Configuracion;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Permitir todas las solicitudes temporalmente
            );

        // Configuración original comentada
        /*
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                // Permitir todas las solicitudes de los endpoints públicos
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/login").permitAll()
                // Asegurar endpoints que requieren roles específicos
                .requestMatchers("/api/administrador/**").hasRole("ADMINISTRADOR") // ROLE_ADMINISTRADOR
                .requestMatchers("/api/usuarios/**").hasAnyRole("USUARIO", "ADMINISTRADOR")
                .requestMatchers("/api/usuario/**").hasAnyRole("USUARIO", "ADMINISTRADOR")
                .requestMatchers("/api/cuentas/**").hasAnyRole("USUARIO", "ADMINISTRADOR")
                .requestMatchers("/api/asientos/**").hasAnyRole("USUARIO", "ADMINISTRADOR")
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .logout(logout -> logout.permitAll()); // Permitir el logout sin restricciones
        */

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Cambia esto según sea necesario
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
