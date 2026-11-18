package com.logistock.app.controller;

import com.logistock.app.model.Producto;
import com.logistock.app.security.UsuarioPrincipal;
import com.logistock.app.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listarTodos(@AuthenticationPrincipal UsuarioPrincipal principal) {
        boolean esAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));
        return esAdmin ? productoService.listarTodos() : productoService.listarPorUsuario(principal.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id,
                                                  @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoService.buscarPorIdYUsuario(Objects.requireNonNull(id), principal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto,
                                                   @AuthenticationPrincipal UsuarioPrincipal principal) {
        Producto nuevoProducto = productoService.crearProducto(producto, principal.getUsuario());
        return ResponseEntity.ok(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto detalles,
                                                         @AuthenticationPrincipal UsuarioPrincipal principal) {
        return productoService.actualizarProducto(id, detalles, principal.getUsuario())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id,
                                                  @AuthenticationPrincipal UsuarioPrincipal principal) {
        if (productoService.eliminarPorId(id, principal.getUsuario())) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
