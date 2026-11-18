package com.logistock.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans de seguridad "de utilidad" sin ninguna dependencia de HttpSecurity,
 * filtros JWT ni AuthenticationProvider.
 *
 * Se separa deliberadamente de {@link SecurityConfig} para que cualquier
 * bean que solo necesite un {@link PasswordEncoder} (como UsuarioService)
 * NO obligue a Spring a construir toda la cadena de filtros de seguridad
 * para obtenerlo. Antes, al vivir el @Bean PasswordEncoder dentro de
 * SecurityConfig, se generaba el siguiente ciclo:
 *
 *   UsuarioService -> PasswordEncoder (bean de SecurityConfig)
 *                  -> requiere instanciar SecurityConfig
 *                  -> requiere JwtAuthenticationFilter
 *                  -> requiere UserDetailsService (UserDetailsServiceImpl)
 *                  -> requiere UsuarioService  (¡vuelta al inicio!)
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
