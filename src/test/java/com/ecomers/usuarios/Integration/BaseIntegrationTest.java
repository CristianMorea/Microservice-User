package com.ecomers.usuarios.Integration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Entitys.UsuarioRolId;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected RolRepository rolRepository;
    @Autowired protected UsuarioRolRepository usuarioRolRepository;
    @Autowired protected PerfilRepository perfilRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtService jwtService;

    @PersistenceContext
    protected EntityManager entityManager; // ← para limpiar caché de Hibernate

    protected Rol rolCliente;
    protected Rol rolAdmin;

    @BeforeEach
    void setUpBase() {
        rolCliente = crearRolSiNoExiste("CLIENTE", 1);
        rolAdmin   = crearRolSiNoExiste("ADMIN", 3);
    }

    protected Rol crearRolSiNoExiste(String nombre, int nivel) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(nombre + " role");
            r.setNivel(nivel);
            r.setCreatedAt(LocalDateTime.now());
            return rolRepository.saveAndFlush(r); // ✅ flush inmediato
        });
    }

    protected Usuario crearUsuario(String email, String password, Rol rol) {
        // 1. Crear y persistir usuario
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.now());
        Usuario guardado = usuarioRepository.saveAndFlush(u); // ✅ flush inmediato

        // 2. Crear Perfil vacío — necesario para que editarPerfil
        // encuentre un perfil existente y no intente crear uno nuevo en caché sucia
        Perfil perfil = new Perfil();
        perfil.setUsuario(guardado);
        perfil.setNombre("");
        perfil.setUpdated_at(LocalDateTime.now());
        perfilRepository.saveAndFlush(perfil);

        // 3. Crear UsuarioRolId con los IDs correctos
        UsuarioRolId urId = new UsuarioRolId();
        urId.setUsuarioId(guardado.getUsuarioId()); // ✅ setear IDs en la clave compuesta
        urId.setRolId(rol.getId());

        // 4. Crear y persistir UsuarioRol
        UsuarioRol ur = new UsuarioRol();
        ur.setId(urId);
        ur.setUsuario(guardado);
        ur.setRol(rol);
        ur.setAssignedBy(guardado);
        ur.setAssignedAt(LocalDateTime.now());
        usuarioRolRepository.saveAndFlush(ur);

        // ✅ Limpia la caché de primer nivel de Hibernate
        // Sin esto, findActiveUserWithRoles devuelve el Usuario sin roles
        // porque Hibernate lo tiene cacheado desde antes de guardar el UsuarioRol
        entityManager.flush();
        entityManager.clear();

        return guardado;
    }

    protected String tokenDe(Usuario usuario) {
        Usuario conRoles = usuarioRepository.findById(usuario.getUsuarioId()).orElseThrow();
        return jwtService.generateToken(conRoles);
    }

    protected String bearerToken(Usuario usuario) {
        return "Bearer " + tokenDe(usuario);
    }
}