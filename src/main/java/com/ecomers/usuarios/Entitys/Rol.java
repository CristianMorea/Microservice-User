package com.ecomers.usuarios.Entitys;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_rol;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    private Integer nivel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rol")
    private Set<UsuarioRol> usuarios = new HashSet<>();
}
