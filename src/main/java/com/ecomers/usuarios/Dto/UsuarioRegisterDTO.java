package com.ecomers.usuarios.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRegisterDTO {
    private String email;
    private String passwordHash;

    // Datos del perfil
    private String nombre;
    private String direccion;
    private String telefono;
    private String avatarUrl;
}
