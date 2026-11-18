package com.logistock.app.security;

import com.logistock.app.model.Rol;
import com.logistock.app.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación de {@link UserDetails} que envuelve la entidad {@link Usuario}
 * completa (no solo email/password/roles como el {@code User} genérico de
 * Spring Security). Esto permite recuperar el usuario autenticado -incluido
 * su id y su plan- directamente en los Controllers con:
 *
 * <pre>
 *   {@literal @}GetMapping("/productos")
 *   public String productos(Model model, {@literal @}AuthenticationPrincipal UsuarioPrincipal principal) {
 *       Usuario usuarioActual = principal.getUsuario();
 *       ...
 *   }
 * </pre>
 *
 * Funciona igual para el flujo de sesión (formLogin/Thymeleaf) y para el
 * flujo JWT, ya que ambos pasan por el mismo UserDetailsServiceImpl.
 */
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getId() {
        return usuario.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles().stream()
                .map(this::aRoleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String aRoleName(Rol rol) {
        String nombre = rol.getNombre().toUpperCase();
        return nombre.startsWith("ROLE_") ? nombre : "ROLE_" + nombre;
    }

    @Override
    public String getPassword() { return usuario.getPassword(); }

    @Override
    public String getUsername() { return usuario.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Un usuario "desactivado" por el admin no puede iniciar sesión.
    @Override
    public boolean isEnabled() { return Boolean.TRUE.equals(usuario.getActivo()); }
}
