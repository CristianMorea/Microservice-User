package com.ecomers.usuarios.Entitys;

import jakarta.persistence.Column;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class UsuarioRolId implements Serializable {

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "rol_id")
    private Integer rolId;
}
