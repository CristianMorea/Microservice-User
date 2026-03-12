package com.ecomers.usuarios.Exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 — Datos inválidos (@Valid en DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        log.warn("Validación fallida: {}", errores);

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Datos inválidos",
                "campos", errores,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — Validación de entidad JPA (@Email, @NotNull en Entity)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        String mensaje = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validación de entidad fallida: {}", mensaje);

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Datos inválidos",
                "detalle", mensaje,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — Violación de integridad (NULL, UNIQUE, FK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.warn("Violación de integridad: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Datos inválidos o incompletos",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — Lógica de negocio inválida
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 404 — Recurso no encontrado
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", 404,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — Conflicto (duplicado)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        log.warn("Conflicto: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 403 — Sin permisos
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 403 — Spring Security AccessDenied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado por Spring Security");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "error", "No tienes permisos para realizar esta acción",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 500 — Error inesperado (siempre al final)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "error", "Error interno del servidor",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}