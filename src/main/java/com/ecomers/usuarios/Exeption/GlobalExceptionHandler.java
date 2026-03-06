package com.ecomers.usuarios.Exeption;



import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Validación del DTO (@Valid) — captura errores ANTES de llegar al service
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            campos.put(campo, mensaje);
        });

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Datos inválidos",
                "campos", campos,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ✅ Validación de la entidad JPA — captura errores al hacer persist
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> campos = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            // propertyPath viene como "metodo.campo", nos quedamos solo con el campo
            String campo = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            campos.put(campo, violation.getMessage());
        });

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Datos inválidos",
                "campos", campos,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Errores de negocio — 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // No autenticado — 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", 401,
                "error", "No autenticado",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Sin permisos — 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "error", "No tienes permisos para realizar esta acción",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Cualquier otro error inesperado — 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "error", "Error interno del servidor",
                "detalle", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}