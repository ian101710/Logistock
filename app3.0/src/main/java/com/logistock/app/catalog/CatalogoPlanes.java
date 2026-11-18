package com.logistock.app.catalog;

import com.logistock.app.model.Plan;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo centralizado de planes de LogiStock.
 *
 * Esta es la ÚNICA fuente de verdad sobre precios y características de cada
 * plan. Tanto la landing pública (index.html) como la pantalla de
 * autoservicio (planes.html) leen de aquí a través de
 * {@link CatalogoPlanesModelAdvice}, así que basta con editar esta clase
 * para que ambos lugares se actualicen automáticamente y jamás queden
 * desincronizados (antes el precio de Premium era distinto en cada vista:
 * S/ 100 en index.html vs. S/ 49 en planes.html).
 *
 * Si en el futuro se prefiere mover esto a base de datos (por ejemplo para
 * que el admin edite precios sin desplegar código), solo hay que cambiar
 * la implementación de {@link #obtenerTodos()} para que lea de un
 * repositorio en vez de la lista fija de abajo; el resto del sistema
 * (vistas, ControllerAdvice) no necesita tocarse.
 */
@Component
public class CatalogoPlanes {

    private final List<PlanInfo> planes = List.of(
            new PlanInfo(
                    Plan.BASICO,
                    "Básico",
                    "S/ 0",
                    "/ mes",
                    List.of("Hasta 50 productos", "Reportes básicos", "1 usuario"),
                    List.of("Alerta de stock crítico", "Multialmacén"),
                    false,
                    "Comenzar Gratis"
            ),
            new PlanInfo(
                    Plan.PREMIUM,
                    "Premium",
                    "S/ 49",
                    "/ mes",
                    List.of("Productos ilimitados", "Reportes básicos y avanzados",
                            "Alerta de stock crítico", "Multialmacén (próximamente)"),
                    List.of(),
                    true,
                    "Contratar Ahora"
            )
    );

    public List<PlanInfo> obtenerTodos() {
        return planes;
    }

    public PlanInfo obtenerPorId(Plan id) {
        return planes.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado en el catálogo: " + id));
    }
}
