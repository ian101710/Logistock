package com.logistock.app.controller;

import com.logistock.app.model.Plan;
import com.logistock.app.service.SolicitudPlanService;
import com.logistock.app.service.UsuarioService;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Panel de control global del superadministrador (ROLE_ADMIN): "Gestión de
 * Cuentas" y bandeja de solicitudes de cambio de plan. Protegido a nivel de
 * URL en SecurityConfig ("/admin/**" -> hasRole("ADMIN")) y reforzado aquí
 * con @PreAuthorize como segunda capa de defensa.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final SolicitudPlanService solicitudPlanService;

    public AdminController(UsuarioService usuarioService, SolicitudPlanService solicitudPlanService) {
        this.usuarioService = usuarioService;
        this.solicitudPlanService = solicitudPlanService;
    }

    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "app/admin_usuarios";
    }

    @PostMapping("/usuarios/{id}/plan")
    public String cambiarPlan(@PathVariable Long id, @RequestParam Plan plan) {
        usuarioService.cambiarPlan(id, plan);
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return "redirect:/admin/usuarios";
    }

    /**
     * Creación de cuentas nuevas (Admin o Usuario) desde el panel.
     * El nombre del rol llega como "ADMIN" o "USER" desde el <select> del
     * formulario (ver admin_usuarios.html).
     */
    @PostMapping("/usuarios/crear")
    public String crearCuenta(@RequestParam String nombreCompleto,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String rol) {
        try {
            usuarioService.crearCuenta(nombreCompleto, email, password, rol);
        } catch (EntityExistsException ex) {
            return "redirect:/admin/usuarios?error=email-duplicado";
        }
        return "redirect:/admin/usuarios?cuenta-creada";
    }

    // ---------------------------------------------------------------
    // Bandeja de solicitudes de cambio de plan (la "notificación" al
    // admin de que un usuario pidió pasar a Premium, o volver a Básico).
    // ---------------------------------------------------------------

    @GetMapping("/solicitudes")
    public String solicitudesDePlan(Model model) {
        model.addAttribute("solicitudes", solicitudPlanService.listarPendientes());
        return "app/admin_solicitudes";
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id) {
        solicitudPlanService.aprobar(id);
        return "redirect:/admin/solicitudes?aprobada";
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id) {
        solicitudPlanService.rechazar(id);
        return "redirect:/admin/solicitudes?rechazada";
    }
}
