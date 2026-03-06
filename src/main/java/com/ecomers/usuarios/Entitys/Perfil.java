package com.ecomers.usuarios.Entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "Perfil")
@Getter
@Setter

public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_perfil;

    @Column(name = "nombre")
    private String nombre;

    private String direccion;
    private String telefono;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "usuario_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Usuario usuario;
}