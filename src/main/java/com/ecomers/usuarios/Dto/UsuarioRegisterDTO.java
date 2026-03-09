package com.ecomers.usuarios.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para registrar un nuevo usuario")
public class UsuarioRegisterDTO {

    @Schema(description = "Email único del usuario", example = "juan@gmail.com")
    private String email;
    @Schema(description = "Contraseña en texto plano — se hashea automáticamente", example = "Pass123!")
    private String passwordHash;

    // Datos del perfil
    @Schema(description = "Nombre completo", example = "Juan Pérez")
    private String nombre;
    @Schema(example = "Calle 123")
    private String direccion;
    @Schema(example = "300123456")
    private String telefono;
    @Schema(description = "URL del avatar", example = "https://img.com/avatar.jpg")
    private String avatarUrl;
}
