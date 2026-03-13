package com.ecomers.usuarios.Exeption;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

    // ── Helper ────────────────────────────────────────────────
    private Map<String, Object> buildError(int status, String error) {
        return new HashMap<>(Map.of(
                "status", status,
                "error", error,
                "timestamp", LocalDateTime.now().toString(),
                "correlationId", MDC.get("correlationId") != null
                        ? MDC.get("correlationId") : "N/A"
        ));
    }

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

        Map<String, Object> body = buildError(400, "Datos inválidos");
        body.put("campos", errores);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 — Validación de entidad JPA (@Email, @NotNull en Entity)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        String mensaje = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validación de entidad fallida: {}", mensaje);

        Map<String, Object> body = buildError(400, "Datos inválidos");
        body.put("detalle", mensaje);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 — Violación de integridad (NULL, UNIQUE, FK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.warn("Violación de integridad: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(400, "Datos inválidos o incompletos"));
    }

    // 400 — Lógica de negocio inválida
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(400, ex.getMessage()));
    }

    // 404 — Recurso no encontrado
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, ex.getMessage()));
    }

    // 409 — Conflicto (duplicado)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        log.warn("Conflicto: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(409, ex.getMessage()));
    }

    // 403 — Sin permisos
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(403, ex.getMessage()));
    }

    // 403 — Spring Security AccessDenied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado por Spring Security");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(403, "No tienes permisos para realizar esta acción"));
    }

    // 500 — Error inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "Error interno del servidor"));
    }
}