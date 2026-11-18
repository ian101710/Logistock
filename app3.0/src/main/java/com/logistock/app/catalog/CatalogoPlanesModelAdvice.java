package com.logistock.app.catalog;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Publica {@code planesCatalogo} como atributo de Model en TODAS las vistas
 * Thymeleaf (index.html, planes.html, etc.) sin que cada método de
 * Controller tenga que agregarlo manualmente. Así, cualquier vista que
 * necesite mostrar los planes simplemente itera sobre
 * {@code ${planesCatalogo}} y automáticamente queda sincronizada con
 * {@link CatalogoPlanes}.
 */
@ControllerAdvice
public class CatalogoPlanesModelAdvice {

    private final CatalogoPlanes catalogoPlanes;

    public CatalogoPlanesModelAdvice(CatalogoPlanes catalogoPlanes) {
        this.catalogoPlanes = catalogoPlanes;
    }

    @ModelAttribute("planesCatalogo")
    public List<PlanInfo> planesCatalogo() {
        return catalogoPlanes.obtenerTodos();
    }
}
