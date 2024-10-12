package com.sa.contable.Configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/administrador/**").hasRole("Administrador")
                .requestMatchers("/api/usuarios/**").hasAnyRole("Usuario", "Administrador")
                .requestMatchers("/api/usuario/**").hasAnyRole("Usuario", "Administrador")
                .requestMatchers("/api/cuentas/**").hasAnyRole("Usuario", "Administrador")
                .requestMatchers("/api/asientos/**").hasAnyRole("Usuario", "Administrador")
                .anyRequest().authenticated()
            )
            .logout(logout -> logout.permitAll())
            .cors();

        http.formLogin().disable();

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
            public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
