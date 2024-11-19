package com.danielflores38153.ms_security.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/security/ggAuthenticator").authenticated() // Requiere autenticación para rutas bajo /loog
                        .anyRequest().permitAll() // Permitir todas las demás solicitudes sin autenticación
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/public/security/auth", true) // Redirige a /api después de un login exitoso
                        .failureUrl("/login?error=true") // Redirige a /login en caso de error
                        .permitAll() // Permite el acceso a la página de inicio de sesión sin autenticación
                );

        return http.build();
    }
}