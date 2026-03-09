package com.ecomers.usuarios.Exeption;


public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}