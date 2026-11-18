package com.logistock.app.repository;
import com.logistock.app.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Categoria findByNombre(String nombre);

    // Categorías visibles para un usuario: las suyas propias + las globales
    // del catálogo (usuario_id IS NULL).
    List<Categoria> findByUsuarioIsNullOrUsuarioId(Long usuarioId);

    // Todas las categorías propias de un usuario (para el aislamiento estricto).
    List<Categoria> findByUsuarioId(Long usuarioId);
}
