package com.logistock.app.service;

import com.logistock.app.model.EstadoSolicitud;
import com.logistock.app.model.Plan;
import com.logistock.app.model.SolicitudCambioPlan;
import com.logistock.app.model.Usuario;
import com.logistock.app.repository.SolicitudCambioPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Orquesta el ciclo de vida de una solicitud de cambio de plan.
 *
 * IMPORTANTE (regla de negocio): un ROLE_USER jamás cambia su propio plan.
 * Solo puede crear una solicitud (ver {@link #solicitarCambio}); el cambio
 * real en {@code Usuario.plan} únicamente ocurre cuando ROLE_ADMIN la
 * aprueba, a través de {@link UsuarioService#cambiarPlan}.
 */
@Service
public class SolicitudPlanService {

    private final SolicitudCambioPlanRepository solicitudRepository;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    public SolicitudPlanService(SolicitudCambioPlanRepository solicitudRepository,
                                 UsuarioService usuarioService,
                                 AuditoriaService auditoriaService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crea una solicitud de cambio de plan para el usuario autenticado.
     * No modifica {@code usuario.plan}: solo dispara la notificación al admin.
     */
    @Transactional
    public SolicitudCambioPlan solicitarCambio(Usuario usuarioActual, Plan planSolicitado) {
        Objects.requireNonNull(planSolicitado);

        if (usuarioActual.getPlan() == planSolicitado) {
            throw new IllegalStateException("Ya cuentas con el plan " + planSolicitado + ".");
        }
        if (solicitudRepository.existsByUsuarioIdAndEstado(usuarioActual.getId(), EstadoSolicitud.PENDIENTE)) {
            throw new IllegalStateException("Ya tienes una solicitud de cambio de plan pendiente de revisión por un administrador.");
        }

        SolicitudCambioPlan solicitud = new SolicitudCambioPlan();
        solicitud.setUsuario(usuarioActual);
        solicitud.setPlanActual(usuarioActual.getPlan());
        solicitud.setPlanSolicitado(planSolicitado);

        SolicitudCambioPlan guardada = solicitudRepository.save(solicitud);
        notificarAdmin(guardada);
        return guardada;
    }

    /**
     * "Notificación" al administrador. Implementación actual: queda registrada
     * en /admin/solicitudes (consulta de PENDIENTES) y además se deja una
     * entrada en el log de auditoría, visible también para el admin.
     *
     * Punto de extensión para correo real: crear una interfaz
     * NotificationService con un método notificar(SolicitudCambioPlan) e
     * inyectar aquí una implementación basada en JavaMailSender (requiere
     * agregar la dependencia spring-boot-starter-mail al pom.xml). No se
     * implementa aquí para no añadir una dependencia de correo que el
     * proyecto no tiene configurada, pero la estructura ya queda lista:
     * basta con llamar a ese servicio desde este único método.
     */
    private void notificarAdmin(SolicitudCambioPlan solicitud) {
        auditoriaService.registrarAccion(
                "SOLICITUD_CAMBIO_PLAN",
                "El usuario " + solicitud.getUsuario().getEmail() + " solicitó cambiar de plan "
                        + solicitud.getPlanActual() + " -> " + solicitud.getPlanSolicitado()
        );
    }

    /** ROLE_ADMIN: bandeja de solicitudes pendientes de revisión. */
    public List<SolicitudCambioPlan> listarPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud.PENDIENTE);
    }

    /** Autoservicio: permite a la vista mostrar si el usuario ya tiene una solicitud en curso. */
    public Optional<SolicitudCambioPlan> buscarPendientePorUsuario(Long usuarioId) {
        return solicitudRepository.findFirstByUsuarioIdAndEstadoOrderByFechaSolicitudDesc(usuarioId, EstadoSolicitud.PENDIENTE);
    }

    /** ROLE_ADMIN: aprueba la solicitud y recién ahí se aplica el cambio real de plan. */
    @Transactional
    public void aprobar(Long solicitudId) {
        SolicitudCambioPlan solicitud = obtener(solicitudId);
        usuarioService.cambiarPlan(solicitud.getUsuario().getId(), solicitud.getPlanSolicitado());

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrarAccion("SOLICITUD_PLAN_APROBADA",
                "Se aprobó el cambio de plan de " + solicitud.getUsuario().getEmail()
                        + " a " + solicitud.getPlanSolicitado());
    }

    /** ROLE_ADMIN: rechaza la solicitud; el plan del usuario no se toca. */
    @Transactional
    public void rechazar(Long solicitudId) {
        SolicitudCambioPlan solicitud = obtener(solicitudId);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrarAccion("SOLICITUD_PLAN_RECHAZADA",
                "Se rechazó el cambio de plan de " + solicitud.getUsuario().getEmail()
                        + " a " + solicitud.getPlanSolicitado());
    }

    private SolicitudCambioPlan obtener(Long id) {
        return solicitudRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));
    }
}
