package com.ecomers.usuarios.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPasswordDTO {

    @Schema(description = "Token JWT actual — se invalida al cambiar password")
    private String tokenActual;
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Debes confirmar la nueva contraseña")
    private String confirmarPassword;
}
