package com.logistock.app.service;

import com.logistock.app.model.Plan;
import com.logistock.app.model.Rol;
import com.logistock.app.model.Usuario;
import com.logistock.app.repository.RolRepository;
import com.logistock.app.repository.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Capa de servicio para la entidad Usuario.
 * Utilizada por la capa de seguridad (UserDetailsServiceImpl) para obtener
 * los datos de autenticación, y por el panel de administración
 * (ROLE_ADMIN) para gestionar cuentas, planes y estado.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    /** ROLE_ADMIN: lista global de usuarios para el panel de administración. */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /** ROLE_ADMIN: sube o baja el plan de un usuario (BASICO <-> PREMIUM). */
    @Transactional
    public Usuario cambiarPlan(Long usuarioId, Plan nuevoPlan) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setPlan(Objects.requireNonNull(nuevoPlan));
        return usuarioRepository.save(usuario);
    }

    /** ROLE_ADMIN: activa o desactiva la cuenta de un usuario. */
    @Transactional
    public Usuario cambiarEstado(Long usuarioId, boolean activo) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    /**
     * ROLE_ADMIN: crea una cuenta nueva (Admin o Usuario) desde el panel de
     * "Gestión de Cuentas". El plan por defecto de toda cuenta nueva es
     * BASICO, y la contraseña se guarda siempre cifrada con BCrypt.
     *
     * @param nombreRol "ADMIN" o "USER" (sin el prefijo ROLE_)
     */
    @Transactional
    public Usuario crearCuenta(String nombreCompleto, String email, String passwordPlano, String nombreRol) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new EntityExistsException("Ya existe una cuenta registrada con ese correo: " + email);
        }

        String rolCompleto = nombreRol.toUpperCase().startsWith("ROLE_") ? nombreRol.toUpperCase() : "ROLE_" + nombreRol.toUpperCase();
        Rol rol = rolRepository.findByNombre(rolCompleto)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + rolCompleto));

        Usuario nuevo = new Usuario();
        nuevo.setNombreCompleto(nombreCompleto);
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(passwordPlano));
        nuevo.setActivo(true);
        nuevo.setPlan(Plan.BASICO);
        nuevo.setRoles(Set.of(rol));

        return usuarioRepository.save(nuevo);
    }

    // NOTA: ya NO existe un método de autoservicio para que el propio usuario
    // cambie su plan. Esa acción ahora pasa siempre por SolicitudPlanService
    // (el usuario solicita, y solo ROLE_ADMIN aplica el cambio real llamando
    // a cambiarPlan(...) más arriba, tras aprobar la solicitud).
}
