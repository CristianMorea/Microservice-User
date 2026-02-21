package com.ecomers.usuarios.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AsignarRolDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String rolNombre;    // "ADMIN", "CLIENTE", "VENDEDOR"
}
