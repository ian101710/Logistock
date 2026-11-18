package com.logistock.app.service;

import com.logistock.app.model.Auditoria;
import com.logistock.app.repository.AuditoriaRepository;
import com.logistock.app.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrarAccion(String accion, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String usuarioEmail = "Sistema";
        Auditoria auditoria = new Auditoria();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            usuarioEmail = auth.getName();
            // Si el principal es nuestro UsuarioPrincipal, enlazamos también
            // la relación @ManyToOne con el usuario (además del email plano).
            if (auth.getPrincipal() instanceof UsuarioPrincipal usuarioPrincipal) {
                auditoria.setUsuario(usuarioPrincipal.getUsuario());
            }
        }

        auditoria.setFecha(LocalDateTime.now());
        auditoria.setUsuarioEmail(usuarioEmail);
        auditoria.setAccion(accion);
        auditoria.setDetalle(detalle);

        auditoriaRepository.save(auditoria);
    }

    /** ROLE_ADMIN: historial completo de auditoría de la plataforma. */
    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }

    /** ROLE_USER: solo su propio historial de auditoría. */
    public List<Auditoria> listarPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}
