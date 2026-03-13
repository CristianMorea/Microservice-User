package com.ecomers.usuarios.Repository;

import com.ecomers.usuarios.Entitys.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //LOGIN

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u " +
            "JOIN FETCH u.roles ur " +
            "JOIN FETCH ur.rol " +
            "LEFT JOIN FETCH u.perfil " +
            "WHERE u.email = :email AND u.isActive = true")
    Optional<Usuario> findActiveUserWithRoles(@Param("email") String email);

    @Query("SELECT u FROM Usuario u " +
            "JOIN FETCH u.roles ur " +
            "JOIN FETCH ur.rol " +
            "LEFT JOIN FETCH u.perfil " +
            "WHERE u.usuarioId = :id")
    Optional<Usuario> findByIdWithDetails(@Param("id") Integer id);

    @Query(value = "SELECT DISTINCT u FROM Usuario u " +
            "JOIN FETCH u.roles ur " +
            "JOIN FETCH ur.rol " +
            "LEFT JOIN FETCH u.perfil " +
            "WHERE u.deletedAt IS NULL",
            countQuery = "SELECT COUNT(DISTINCT u) FROM Usuario u " +
                    "WHERE u.deletedAt IS NULL")
    Page<Usuario> findAllActivosWithDetails(Pageable pageable);
}
