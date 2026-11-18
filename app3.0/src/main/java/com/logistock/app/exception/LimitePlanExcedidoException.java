package com.logistock.app.exception;

/**
 * Se lanza cuando un usuario con plan {@code BASICO} intenta superar los
 * límites de su plan (por ejemplo, crear más de 50 productos).
 * El GlobalExceptionHandler la traduce a HTTP 400 en la API REST; en la
 * capa web (Thymeleaf) el WebController la captura y redirige mostrando
 * un aviso de "Mejora tu plan".
 */
public class LimitePlanExcedidoException extends RuntimeException {

    public LimitePlanExcedidoException(String message) {
        super(message);
    }
}
