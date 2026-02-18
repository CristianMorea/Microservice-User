package com.ecomers.usuarios.Repository;

import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Entitys.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByUsuarioUsuarioId(Integer usuarioId);
}
