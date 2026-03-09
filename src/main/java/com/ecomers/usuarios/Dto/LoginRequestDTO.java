package com.ecomers.usuarios.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Credenciales de login")
public class LoginRequestDTO {
    @Schema(example = "juan@gmail.com")
    private String email;
    @Schema(example = "Pass123!")
    private String password;
}
