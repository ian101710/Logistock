package com.logistock.app.model;

/**
 * Planes de suscripción disponibles para un {@link Usuario} dentro del
 * modelo SaaS de LogiStock.
 *
 * BASICO: límite de 50 productos por usuario, sin alerta de stock crítico.
 * PREMIUM: productos ilimitados + alerta de stock crítico (y a futuro,
 *          soporte de multialmacén).
 */
public enum Plan {
    BASICO,
    PREMIUM
}
