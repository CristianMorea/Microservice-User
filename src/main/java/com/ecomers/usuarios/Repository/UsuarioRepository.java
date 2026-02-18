package com.ecomers.usuarios.Repository;

import com.ecomers.usuarios.Entitys.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //LOGIN
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndActiveTrue(String email);
    Optional<Usuario> findByEmailAndIsActiveTrueAndDeletedAtIsNull(String email);


    //VALIDACIONES REGISTRO
    boolean existsByEmail(String email);

    //VALIDACIONES REGISTRO
    List<Usuario> findAllByIsActiveTrueAndDeletedAtIsNull();

    //SOF DELETE
    List<Usuario> findAllByDeletedAtIsNull();

    // 🔐 CARGAR USUARIO CON ROLES (MUY IMPORTANTE)
    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN FETCH u.roles ur
        LEFT JOIN FETCH ur.rol
        WHERE u.email = :email
        AND u.isActive = true
        AND u.deletedAt IS NULL
    """)
    Optional<Usuario> findActiveUserWithRoles(@Param("email") String email);



}
