package com.logistock.app.catalog;

import com.logistock.app.model.Plan;

import java.util.List;

/**
 * Datos "de vitrina" de un plan (precio, características, etc.), separados
 * deliberadamente de la entidad {@link Plan} (que solo es el enum usado en
 * la base de datos). Esto es lo que consumen index.html y planes.html para
 * pintar las tarjetas, así que ambos siempre muestran exactamente lo mismo.
 */
public record PlanInfo(
        Plan id,
        String nombre,
        String precio,
        String periodo,
        List<String> incluye,
        List<String> noIncluye,
        boolean destacado,
        String textoBoton
) {
}
