package com.logistock.app.service;

import com.logistock.app.model.Categoria;
import com.logistock.app.model.Usuario;
import com.logistock.app.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Capa de servicio para la entidad Categoria.
 * Un usuario ve las categorías "globales" del catálogo (sembradas por el
 * sistema, usuario_id = NULL) más las que él mismo haya creado.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** ROLE_ADMIN: ve todas las categorías de la plataforma. */
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    /** ROLE_USER: categorías globales + propias. */
    public List<Categoria> listarVisiblesPara(Long usuarioId) {
        return categoriaRepository.findByUsuarioIsNullOrUsuarioId(Objects.requireNonNull(usuarioId));
    }

    public Categoria buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    /** Crea una categoría privada perteneciente al usuario actual. */
    public Categoria crearParaUsuario(String nombre, Usuario usuarioActual) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setUsuario(usuarioActual);
        return categoriaRepository.save(categoria);
    }
}
