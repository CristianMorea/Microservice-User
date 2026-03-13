package com.ecomers.usuarios.Dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Respuesta tras registrar un usuario")
public class UsuarioRegisterResponseDTO {

    @Schema(description = "ID del usuario creado", example = "1")
    private Integer usuarioId;

    @Schema(description = "Email registrado", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String nombre;
}
