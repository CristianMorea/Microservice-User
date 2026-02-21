package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Entitys.UsuarioRol;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;



public interface RolService {
    void asignar(Integer usuarioId, String rolNombre);


    void quitar(Integer usuarioId, String rolNombre);
}
