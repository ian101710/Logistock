package com.logistock.app.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de excepciones para la API REST (paquete controller: /api/**).
 * Se limita a los controladores REST para no alterar el comportamiento de la
 * interfaz web basada en Thymeleaf (WebController), que sigue devolviendo
 * vistas HTML y redirecciones como antes.
 *
 * Formato uniforme de respuesta:
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "message": "Validation Error",
 *   "errors": { "nombre": "No puede estar vacío" }
 * }
 */
@RestControllerAdvice(basePackages = "com.logistock.app.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Validation Error", errores);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(LimitePlanExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleLimitePlanExcedido(LimitePlanExcedidoException ex) {
        // 402 Payment Required: comunica claramente al frontend que el
        // bloqueo es por límite de plan, para que muestre "Mejora tu plan".
        return construirRespuesta(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado", null);
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String message,
                                                                     Map<String, String> errores) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        if (errores != null && !errores.isEmpty()) {
            body.put("errors", errores);
        }
        return new ResponseEntity<>(body, status);
    }
}
