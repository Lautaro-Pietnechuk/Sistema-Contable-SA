package com.sa.contable.Configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaDeFiltrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll() // Acceso público
                .requestMatchers("/api/administrador/**").hasRole("Administrador") // Solo ADMIN
                .requestMatchers("/api/usuario/**").hasAnyRole("Usuario", "Administrador") // USER o ADMIN
                .anyRequest().authenticated() // Autenticación para todas las demás rutas
            )
            .formLogin(form -> form
                .loginPage("/login") // Página de inicio de sesión personalizada
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            );

        return http.build();
    }

    // Definir el AuthenticationManager como un Bean
    @Bean
    public AuthenticationManager ManagerAutentificacion(AuthenticationConfiguration ConfiguracionAutentificacion) throws Exception {
        return ConfiguracionAutentificacion.getAuthenticationManager();
    }

    // Codificador de contraseñas para almacenar las contraseñas de manera segura
    /* @Bean
    public PasswordEncoder CodificadoContraseñas() {
        return new BCryptPasswordEncoder();
    } */ // No obligatorio
}
