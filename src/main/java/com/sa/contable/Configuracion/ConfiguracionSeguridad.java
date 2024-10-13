package com.sa.contable.Configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ConfiguracionSeguridad {

    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain cadenaDeFiltrosDeSeguridad(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/administrador/**").hasRole("Administrador")
                .requestMatchers("/api/usuarios/**").hasAnyRole("Usuario", "Administrador")
                .requestMatchers("/api/usuario/**").hasAnyRole("Usuario", "Administrador")
                .requestMatchers("/api/cuentas/**").hasRole("Administrador")
                .requestMatchers("/api/asientos/**").hasAnyRole("Usuario", "Administrador")
                .anyRequest().authenticated()
            )
            .logout(logout -> logout.permitAll());

        // Habilitar CORS
        http.cors(); 

        return http.build();
    }

    @Bean
    public AuthenticationManager ManagerAutentificacion(AuthenticationConfiguration ConfiguracionAutentificacion) throws Exception {
        return ConfiguracionAutentificacion.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000") // Origen permitido
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                    .allowedHeaders("*") // Cabeceras permitidas
                    .allowCredentials(true); // Permitir credenciales (cookies, autorización)
            }
        };
    }
}
