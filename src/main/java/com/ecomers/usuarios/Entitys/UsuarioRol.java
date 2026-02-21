package com.ecomers.usuarios.Entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_rol")
@Getter
@Setter
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id = new UsuarioRolId();

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Usuario assignedBy;


    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
