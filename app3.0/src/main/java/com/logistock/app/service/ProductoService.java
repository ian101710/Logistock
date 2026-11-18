package com.logistock.app.service;

import com.logistock.app.exception.LimitePlanExcedidoException;
import com.logistock.app.model.Plan;
import com.logistock.app.model.Producto;
import com.logistock.app.model.Usuario;
import com.logistock.app.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Capa de servicio para la entidad Producto.
 * Concentra toda la lógica de negocio relacionada con productos e inventario,
 * incluyendo el aislamiento de datos por usuario (multitenancy) y las
 * validaciones de los límites del plan de suscripción (SaaS).
 */
@Service
public class ProductoService {

    /** Límite de productos para el plan BASICO (Requerimiento de negocio). */
    public static final int LIMITE_PRODUCTOS_PLAN_BASICO = 50;

    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    public ProductoService(ProductoRepository productoRepository, AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.auditoriaService = auditoriaService;
    }

    // ---------------------------------------------------------------
    // Lectura (siempre acotada al usuario dueño de los datos)
    // ---------------------------------------------------------------

    /** ROLE_ADMIN: ve todo el inventario de la plataforma. */
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    /** ROLE_USER: solo ve su propio inventario. */
    public List<Producto> listarPorUsuario(Long usuarioId) {
        return productoRepository.findByUsuarioId(Objects.requireNonNull(usuarioId));
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(Objects.requireNonNull(id));
    }

    /** Busca un producto garantizando que pertenece al usuario indicado. */
    public Optional<Producto> buscarPorIdYUsuario(Long id, Long usuarioId) {
        return productoRepository.findByIdAndUsuarioId(Objects.requireNonNull(id), Objects.requireNonNull(usuarioId));
    }

    public List<Producto> listarStockCritico(Long usuarioId) {
        List<Producto> criticos = productoRepository.findByUsuarioIdAndCantidadLessThan(usuarioId, 5);
        auditoriaService.registrarAccion("VISTA_CRITICA", "Acceso a panel crítico");
        return criticos;
    }

    // ---------------------------------------------------------------
    // Escritura (valida propiedad del dato + límites del plan)
    // ---------------------------------------------------------------

    /** Determina si el usuario tiene ROLE_ADMIN (control total sobre cualquier cuenta). */
    private boolean esAdmin(Usuario usuario) {
        return usuario.getRoles().stream().anyMatch(r -> r.getNombre().toUpperCase().contains("ADMIN"));
    }

    /**
     * Busca el producto que se va a modificar, respetando el aislamiento por
     * usuario para ROLE_USER pero permitiendo a ROLE_ADMIN gestionar
     * cualquier cuenta (requisito de "Gestión Administrativa").
     */
    private Producto localizarParaEscritura(Long id, Usuario usuarioActual) {
        if (esAdmin(usuarioActual)) {
            return productoRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("El producto no existe"));
        }
        return productoRepository.findByIdAndUsuarioId(id, usuarioActual.getId())
                .orElseThrow(() -> new IllegalStateException("El producto no existe o no te pertenece"));
    }

    /**
     * Guarda (crea o edita) un producto perteneciente a {@code usuarioActual},
     * aplicando la validación del límite del plan BASICO al crear.
     * Usado por la vista Thymeleaf (WebController). ROLE_ADMIN puede editar
     * productos de cualquier usuario (no solo los propios).
     */
    public Producto guardarProducto(Producto producto, Usuario usuarioActual) {
        boolean esNuevo = (producto.getId() == null);

        if (esNuevo) {
            validarLimitePlan(usuarioActual);
            producto.setUsuario(usuarioActual);
        } else {
            // En edición, se conserva siempre al dueño original (nunca se
            // reasigna el producto a otro usuario desde el formulario,
            // ni siquiera cuando quien edita es el admin).
            Producto existente = localizarParaEscritura(producto.getId(), usuarioActual);
            producto.setUsuario(existente.getUsuario());
        }

        Producto guardado = productoRepository.save(producto);

        String accion = esNuevo ? "CREAR_PRODUCTO" : "EDITAR_PRODUCTO";
        auditoriaService.registrarAccion(accion, "Producto: " + guardado.getNombre());
        return guardado;
    }

    /**
     * Valida que el usuario, si tiene plan BASICO, no supere el límite de
     * {@value #LIMITE_PRODUCTOS_PLAN_BASICO} productos. El plan PREMIUM no
     * tiene restricción.
     */
    private void validarLimitePlan(Usuario usuarioActual) {
        if (usuarioActual.getPlan() == Plan.PREMIUM) {
            return;
        }
        long totalActual = productoRepository.countByUsuarioId(usuarioActual.getId());
        if (totalActual >= LIMITE_PRODUCTOS_PLAN_BASICO) {
            throw new LimitePlanExcedidoException(
                "Has alcanzado el límite de " + LIMITE_PRODUCTOS_PLAN_BASICO +
                " productos de tu plan BASICO. Mejora a Premium para productos ilimitados.");
        }
    }

    /**
     * Registra un movimiento de inventario (ingreso, venta, merma, defectuoso)
     * y actualiza el stock del producto correspondiente. ROLE_USER solo
     * puede operar sobre sus propios productos; ROLE_ADMIN puede operar
     * sobre cualquier producto de la plataforma.
     */
    public void registrarMovimiento(Long id, String tipoMovimiento, int cantidad, Usuario usuarioActual) {
        Long safeId = Objects.requireNonNull(id);
        Optional<Producto> productoOpt = esAdmin(usuarioActual)
                ? productoRepository.findById(safeId)
                : productoRepository.findByIdAndUsuarioId(safeId, usuarioActual.getId());

        productoOpt.ifPresent(p -> {
            int stockAnterior = p.getCantidad();
            if ("INGRESO".equals(tipoMovimiento)) {
                p.setCantidad(p.getCantidad() + cantidad);
            } else {
                p.setCantidad(Math.max(0, p.getCantidad() - cantidad));
            }
            productoRepository.save(p);
            String detalle = String.format("Tipo: %s | Prod: %s | Stock: %d -> %d",
                    tipoMovimiento, p.getNombre(), stockAnterior, p.getCantidad());
            auditoriaService.registrarAccion("MOVIMIENTO_INVENTARIO", detalle);
        });
    }

    /**
     * Elimina un producto (flujo de la vista Thymeleaf) registrando auditoría.
     * ROLE_USER solo puede eliminar sus propios productos; ROLE_ADMIN puede
     * eliminar cualquier producto de la plataforma.
     */
    public void eliminarProductoConAuditoria(Long id, Usuario usuarioActual) {
        Long safeId = Objects.requireNonNull(id);
        Optional<Producto> productoOpt = esAdmin(usuarioActual)
                ? productoRepository.findById(safeId)
                : productoRepository.findByIdAndUsuarioId(safeId, usuarioActual.getId());

        productoOpt.ifPresent(p -> {
            productoRepository.deleteById(safeId);
            auditoriaService.registrarAccion("ELIMINAR_PRODUCTO", "Eliminado: " + p.getNombre());
        });
    }

    /**
     * Crea un producto (flujo de la API REST), validando el límite de plan.
     */
    public Producto crearProducto(Producto producto, Usuario usuarioActual) {
        validarLimitePlan(usuarioActual);
        producto.setUsuario(usuarioActual);
        return productoRepository.save(Objects.requireNonNull(producto));
    }

    /**
     * Actualiza un producto existente (flujo de la API REST), solo si
     * pertenece al usuario actual.
     */
    public Optional<Producto> actualizarProducto(Long id, Producto detalles, Usuario usuarioActual) {
        return productoRepository.findByIdAndUsuarioId(Objects.requireNonNull(id), usuarioActual.getId()).map(producto -> {
            producto.setNombre(detalles.getNombre());
            producto.setCantidad(detalles.getCantidad());
            producto.setPrecioCompra(detalles.getPrecioCompra());
            producto.setPrecioVenta(detalles.getPrecioVenta());
            producto.setCategoria(detalles.getCategoria());
            return productoRepository.save(producto);
        });
    }

    /**
     * Elimina un producto por id (flujo de la API REST), solo si pertenece
     * al usuario actual. Devuelve true si existía y se eliminó.
     */
    public boolean eliminarPorId(Long id, Usuario usuarioActual) {
        Long safeId = Objects.requireNonNull(id);
        return productoRepository.findByIdAndUsuarioId(safeId, usuarioActual.getId())
                .map(p -> {
                    productoRepository.deleteById(safeId);
                    return true;
                })
                .orElse(false);
    }
}
