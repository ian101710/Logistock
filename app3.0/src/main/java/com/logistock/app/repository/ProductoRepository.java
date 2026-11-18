package com.logistock.app.repository;
import com.logistock.app.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring Data JPA crea la query automáticamente
    List<Producto> findByCantidadLessThan(Integer cantidad);

    // --- Aislamiento por usuario (multitenancy) ---
    List<Producto> findByUsuarioId(Long usuarioId);

    Optional<Producto> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Producto> findByUsuarioIdAndCantidadLessThan(Long usuarioId, Integer cantidad);

    // Usado por ProductoService para validar el límite del plan BASICO (máx. 50).
    long countByUsuarioId(Long usuarioId);
}
