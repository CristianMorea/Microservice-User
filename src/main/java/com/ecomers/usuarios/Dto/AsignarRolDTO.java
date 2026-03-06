package com.ecomers.usuarios.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class AsignarRolDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String rolNombre;    // "ADMIN", "CLIENTE", "VENDEDOR"
}
