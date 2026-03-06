package com.ecomers.usuarios.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PerfilResponseDTO {

    private Integer usuarioId;
    private String email;
    private String nombre;
    private String direccion;
    private String telefono;
    private String avatarUrl;
    private List<String> rol;
}
