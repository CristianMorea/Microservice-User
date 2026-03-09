package com.ecomers.usuarios.Exeption;


public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) {
        super(mensaje);
    }
}