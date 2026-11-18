package com.logistock.app;

import com.logistock.app.exception.LimitePlanExcedidoException;
import com.logistock.app.model.Producto;
import com.logistock.app.model.Usuario;
import com.logistock.app.security.UsuarioPrincipal;
import com.logistock.app.service.AuditoriaService;
import com.logistock.app.service.CategoriaService;
import com.logistock.app.service.ProductoService;
import com.logistock.app.service.SolicitudPlanService;
import com.logistock.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WebController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;
    private final SolicitudPlanService solicitudPlanService;

    public WebController(ProductoService productoService,
                          CategoriaService categoriaService,
                          AuditoriaService auditoriaService,
                          UsuarioService usuarioService,
                          SolicitudPlanService solicitudPlanService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
        this.solicitudPlanService = solicitudPlanService;
    }

    @GetMapping({"/", "/index"})
    public String index() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    // ---------------------------------------------------------------
    // Registro público (autoservicio): toda cuenta nueva nace ROLE_USER
    // + plan BASICO (reutiliza UsuarioService.crearCuenta, la misma lógica
    // que ya usa el admin para crear cuentas desde /admin/usuarios).
    // ---------------------------------------------------------------

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @PostMapping("/registro")
    public String registrarCuenta(@RequestParam String nombreCompleto,
                                   @RequestParam String email,
                                   @RequestParam String password) {
        if (nombreCompleto == null || nombreCompleto.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 6) {
            return "redirect:/registro?error=datos-invalidos";
        }
        try {
            usuarioService.crearCuenta(nombreCompleto, email, password, "USER");
        } catch (jakarta.persistence.EntityExistsException ex) {
            return "redirect:/registro?error=email-duplicado";
        }
        return "redirect:/login?registrado";
    }

    /**
     * Helper: obtiene los productos que corresponden ver al usuario logueado.
     * ROLE_ADMIN ve todo el inventario de la plataforma; ROLE_USER solo el suyo.
     */
    private List<Producto> productosVisiblesPara(Usuario usuario) {
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().toUpperCase().contains("ADMIN"));
        return esAdmin ? productoService.listarTodos() : productoService.listarPorUsuario(usuario.getId());
    }

    // 1. DASHBOARD: Solo métricas y resumen (Separado del almacén)
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuarioActual = principal.getUsuario();
        List<Producto> productos = productosVisiblesPara(usuarioActual);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.listarVisiblesPara(usuarioActual.getId()));
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("limiteBasico", ProductoService.LIMITE_PRODUCTOS_PLAN_BASICO);
        return "app/dashboard";
    }

    // 2. ALMACÉN: Gestión completa de productos
    @GetMapping("/productos")
    public String productos(Model model, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuarioActual = principal.getUsuario();
        model.addAttribute("productos", productosVisiblesPara(usuarioActual));
        model.addAttribute("categorias", categoriaService.listarVisiblesPara(usuarioActual.getId()));
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("limiteBasico", ProductoService.LIMITE_PRODUCTOS_PLAN_BASICO);
        return "app/productos";
    }

    // 3. AUDITORÍA: Recuperada y conectada
    @GetMapping("/auditoria")
    public String auditoria(Model model, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuarioActual = principal.getUsuario();
        boolean esAdmin = usuarioActual.getRoles().stream()
                .anyMatch(r -> r.getNombre().toUpperCase().contains("ADMIN"));
        model.addAttribute("auditorias", esAdmin
                ? auditoriaService.listarTodas()
                : auditoriaService.listarPorUsuario(usuarioActual.getId()));
        auditoriaService.registrarAccion("CONSULTA", "El usuario visualizó el registro de auditoría");
        return "app/auditoria";
    }

    // Guardar o Editar Producto
    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute Producto producto, BindingResult result,
                                   @AuthenticationPrincipal UsuarioPrincipal principal) {
        if (result.hasErrors()) return "redirect:/productos?error=validacion";

        try {
            productoService.guardarProducto(producto, principal.getUsuario());
        } catch (LimitePlanExcedidoException ex) {
            // El frontend detecta este parámetro para mostrar el aviso "Mejora tu plan".
            return "redirect:/productos?error=limite-plan";
        }
        return "redirect:/productos";
    }

    // 4. Lógica de Movimientos (Ingreso, Venta, Merma, Defectuoso)
    @PostMapping("/productos/{id}/movimiento")
    public String registrarMovimiento(@PathVariable Long id,
                                      @RequestParam String tipoMovimiento,
                                      @RequestParam int cantidad,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        productoService.registrarMovimiento(id, tipoMovimiento, cantidad, principal.getUsuario());
        return "redirect:/productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        productoService.eliminarProductoConAuditoria(id, principal.getUsuario());
        return "redirect:/productos";
    }

    // La Alerta de Stock Crítico es un beneficio exclusivo del plan PREMIUM.
    @GetMapping("/stock-critico")
    public String stockCritico(Model model, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuarioActual = principal.getUsuario();
        boolean esAdmin = usuarioActual.getRoles().stream()
                .anyMatch(r -> r.getNombre().toUpperCase().contains("ADMIN"));
        if (!esAdmin && !usuarioActual.esPremium()) {
            return "redirect:/planes?aviso=premium-requerido";
        }
        model.addAttribute("criticos", esAdmin
                ? productoService.listarTodos().stream().filter(p -> p.getCantidad() < 5).toList()
                : productoService.listarStockCritico(usuarioActual.getId()));
        return "app/stock_critico";
    }

    // ---------------------------------------------------------------
    // Selección / autoservicio de planes
    // ---------------------------------------------------------------

    @GetMapping("/planes")
    public String planes(Model model, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuarioActual = principal.getUsuario();
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("solicitudPendiente",
                solicitudPlanService.buscarPendientePorUsuario(usuarioActual.getId()).orElse(null));
        return "app/planes";
    }

    /**
     * El usuario YA NO cambia su plan directamente. Esto únicamente crea una
     * solicitud pendiente; el cambio real solo lo aplica ROLE_ADMIN al
     * aprobarla desde /admin/solicitudes (ver AdminController).
     */
    @PostMapping("/planes/solicitar")
    public String solicitarCambioPlan(@RequestParam com.logistock.app.model.Plan planSolicitado,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        try {
            solicitudPlanService.solicitarCambio(principal.getUsuario(), planSolicitado);
            return "redirect:/planes?solicitud-enviada";
        } catch (IllegalStateException ex) {
            return "redirect:/planes?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
