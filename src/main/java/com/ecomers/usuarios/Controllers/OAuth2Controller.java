package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Entitys.Usuario;

import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @GetMapping("/usuarios/oauth2/success")
    public String oauth2Success(OAuth2User principal) {

        String email = principal.getAttribute("email");
        String nombre = principal.getAttribute("name");

        // Buscar o crear usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    nuevo.setActive(true);
                    nuevo.setCreatedAt(LocalDateTime.now());
                    return usuarioRepository.save(nuevo);
                });

        // Generar JWT para comunicación interna
        String token = jwtService.generateToken(usuario);

        return "Login exitoso con OAuth2. Token: " + token;
    }

    @GetMapping("/usuarios/oauth2/failure")
    public String oauth2Failure() {
        return "Falló la autenticación con OAuth2";
    }
}
