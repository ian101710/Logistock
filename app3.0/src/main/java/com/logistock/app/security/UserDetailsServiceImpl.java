package com.logistock.app.security;

import com.logistock.app.model.Usuario;
import com.logistock.app.service.UsuarioService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioService usuarioService;

    public UserDetailsServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + email));

        // Se envuelve la entidad completa para que esté disponible en los
        // Controllers vía @AuthenticationPrincipal UsuarioPrincipal.
        return new UsuarioPrincipal(usuario);
    }
}
