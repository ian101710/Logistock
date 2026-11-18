package com.logistock.app.security;

import com.logistock.app.security.jwt.JwtAuthenticationEntryPoint;
import com.logistock.app.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad con DOS cadenas de filtros independientes:
 *
 * 1) apiFilterChain (/api/**): autenticación STATELESS mediante JWT.
 *    Se usa para la API REST (ProductoRestController, AuthController).
 *
 * 2) webFilterChain (todo lo demás): se mantiene el login por formulario
 *    y sesión (formLogin) que utiliza la interfaz Thymeleaf/Bootstrap
 *    (login.html, dashboard, productos, auditoria, etc.), para no alterar
 *    el frontend existente.
 *
 * Ambas cadenas comparten el mismo AuthenticationProvider (DAO + BCrypt),
 * por lo que las credenciales de Usuario funcionan igual en ambos flujos.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    // ---------- Cadena 1: API REST protegida con JWT (STATELESS) ----------
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ---------- Cadena 2: Interfaz web Thymeleaf/Bootstrap (formLogin + sesión) ----------
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permitimos acceso total a recursos estáticos para que el estilo no se rompa
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/registro", "/", "/index").permitAll()
                // Panel de administración: exclusivo para ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout=true").permitAll());

        return http.build();
    }

    // El bean PasswordEncoder ya NO vive aquí: fue movido a SecurityBeansConfig
    // (ver esa clase para el detalle de por qué era necesario separarlo).

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService,
                                                           PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
