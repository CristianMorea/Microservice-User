package com.ecomers.usuarios.Dto;

import lombok.*;

@Getter
@Setter
@Builder
public class LoginResponseDTO {

    private Integer usuarioId;
    private String email;

    private String nombre;
    private String rol;
}
