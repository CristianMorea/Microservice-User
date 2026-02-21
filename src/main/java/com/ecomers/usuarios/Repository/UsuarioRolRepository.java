package com.ecomers.usuarios.Repository;

import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Entitys.UsuarioRolId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    boolean existsByUsuario_UsuarioIdAndRol_Nombre(Integer usuarioId, String rolNombre);

    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByUsuario_UsuarioIdAndRol_Nombre(Integer usuarioId, String rolNombre);
    Optional<UsuarioRol> findByUsuario_UsuarioIdAndRol_Nombre(Integer usuarioId, String rolNombre);


}
