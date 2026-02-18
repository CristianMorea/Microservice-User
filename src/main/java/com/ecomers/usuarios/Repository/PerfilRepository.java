package com.ecomers.usuarios.Repository;

import com.ecomers.usuarios.Entitys.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByUsuarioUsuarioId(Integer usuarioId);
}
