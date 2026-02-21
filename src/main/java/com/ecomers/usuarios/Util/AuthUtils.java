package com.ecomers.usuarios.Util;

import com.ecomers.usuarios.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {
    private final JwtService jwtService;

    public AuthUtils(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // ✅ Obtiene el email del usuario autenticado desde el SecurityContext
    public String getEmailActual() {
        return (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // ✅ Obtiene el usuarioId desde el token en el request
    public Integer getUsuarioIdActual(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.obtenerUsuarioIdDesdeToken(token);
    }

}
