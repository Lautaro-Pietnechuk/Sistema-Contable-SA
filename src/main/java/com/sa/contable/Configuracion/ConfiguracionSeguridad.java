package com.sa.contable.Configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaDeFiltrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/login").permitAll() // Permitir acceso sin autenticación a /api/login
                .requestMatchers("/api/administrador/**").hasRole("Administrador")
                .requestMatchers("/api/usuario/**").hasAnyRole("Usuario", "Administrador")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/") // Cambia la URL de procesamiento del formulario de inicio de sesión
                .successHandler((request, response, authentication) -> {
                    response.setStatus(200); // Cambia el código de estado a 200 si el inicio de sesión es exitoso
                    response.getWriter().write("{\"message\":\"Login exitoso\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401); // Cambia el código de estado a 401 si el inicio de sesión falla
                    response.getWriter().write("{\"error\":\"Usuario o contraseña incorrectos\"}");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager ManagerAutentificacion(AuthenticationConfiguration ConfiguracionAutentificacion) throws Exception {
        return ConfiguracionAutentificacion.getAuthenticationManager();
    }
}
