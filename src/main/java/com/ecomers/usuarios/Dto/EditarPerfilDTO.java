package com.ecomers.usuarios.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarPerfilDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String telefono;    // opcional
    private String direccion;   // opcional
}
