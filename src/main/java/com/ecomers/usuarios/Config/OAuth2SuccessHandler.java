package com.ecomers.usuarios.Config;

import java.io.IOException;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    nuevo.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

                    Perfil perfil = new Perfil();
                    perfil.setNombre(oAuth2User.getAttribute("name"));
                    perfil.setUsuario(nuevo); // ✅ asocias el perfil al usuario
                    nuevo.setPerfil(perfil);  // ✅ asocias el usuario al perfil

                    return usuarioRepository.save(nuevo);
                });

        String token = jwtService.generateToken(usuario);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
    }
}