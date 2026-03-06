package com.ecomers.usuarios.Config;

import java.io.IOException;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.TestUnitarios.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");

        //  Busca con roles cargados para que el token los incluya
        Usuario usuario = usuarioRepository.findActiveUserWithRoles(email)
                .orElseGet(() -> crearUsuarioDesdeOAuth2(email, nombre));

        String token = jwtService.generateToken(usuario);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
            {
                "token": "%s",
                "email": "%s",
                "nombre": "%s"
            }
        """.formatted(token, usuario.getEmail(),
                usuario.getPerfil() != null ? usuario.getPerfil().getNombre() : ""));
    }

    private Usuario crearUsuarioDesdeOAuth2(String email, String nombre) {
        // 1. Crear usuario
        Usuario nuevo = new Usuario();
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        nuevo.setActive(true);
        nuevo.setCreatedAt(LocalDateTime.now());
        Usuario usuarioGuardado = usuarioRepository.saveAndFlush(nuevo);

        // 2. Crear perfil
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuarioGuardado);
        perfil.setNombre(nombre);
        perfil.setUpdated_at(LocalDateTime.now());
        perfilRepository.saveAndFlush(perfil);

        // 3. Asignar rol CLIENTE ✅
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no existe"));

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuarioGuardado);
        usuarioRol.setRol(rolCliente);
        usuarioRol.setAssignedBy(usuarioGuardado);
        usuarioRol.setAssignedAt(LocalDateTime.now());
        usuarioRolRepository.save(usuarioRol);

        // 4. Recargar con roles para que el token los incluya ✅
        return usuarioRepository.findActiveUserWithRoles(email)
                .orElseThrow(() -> new RuntimeException("Error al crear usuario OAuth2"));
    }
}