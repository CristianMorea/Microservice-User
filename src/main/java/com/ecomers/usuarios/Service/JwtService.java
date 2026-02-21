package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Entitys.Usuario;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface JwtService {
    String generateToken(Usuario usuario);
    boolean validateToken(String token);
    String obtenerEmailDesdeToken(String token);
    List<GrantedAuthority> obtenerAuthoritiesDesdeToken(String token);
    Integer obtenerUsuarioIdDesdeToken(String token);
}
