package com.ecomers.usuarios.Config;

import com.ecomers.usuarios.TestUnitarios.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final List<String> PUBLIC_URLS = List.of(
            "/usuarios/login",
            "/usuarios/register",
            "/oauth2/",
            "/login/oauth2/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // ✅ Si no hay token, continúa sin autenticar (Spring Security rechazará si la ruta lo requiere)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return; // 👈 return temprano, evita el else anidado
        }

        String token = authHeader.substring(7);

        // ✅ Valida primero antes de intentar parsear
        if (!jwtService.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Solo se ejecuta si aún no hay autenticación en el contexto
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtService.obtenerEmailDesdeToken(token);
            List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);
            // 👆 Roles desde el token — sin tocar la BD

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            // 👆 principal es el email (String), suficiente para la mayoría de casos

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        chain.doFilter(request, response);
    }
}