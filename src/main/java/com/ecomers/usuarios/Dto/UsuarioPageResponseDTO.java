package com.ecomers.usuarios.Dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UsuarioPageResponseDTO {

    private List<PerfilResponseDTO> usuarios;
    private int paginaActual;
    private int totalPaginas;
    private long totalUsuarios;
    private int tamañoPagina;
    private boolean esUltimaPagina;
}

