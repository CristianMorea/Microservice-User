package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Entitys.Usuario;

public interface JwtService {
    String generateToken(Usuario usuario);
    boolean validateToken(String token);
    String obtenerEmailDesdeToken(String token);
}
