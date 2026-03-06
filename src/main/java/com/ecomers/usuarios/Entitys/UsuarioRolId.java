package com.ecomers.usuarios.Entitys;

import jakarta.persistence.Column;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@Data
public class UsuarioRolId implements Serializable {

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "rol_id")
    private Integer rolId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioRolId)) return false;
        UsuarioRolId that = (UsuarioRolId) o;
        return Objects.equals(usuarioId, that.usuarioId) &&
                Objects.equals(rolId, that.rolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, rolId);
    }
}
