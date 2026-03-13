package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Config.JwtConfig;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;

    // ── generateToken ─────────────────────────────────────────
    // NO se cachea — cada llamada genera un token nuevo
    @Override
    public String generateToken(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getUsuarioId())
                .claim("roles", String.join(",", roles))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getKey())
                .compact();
    }

    // ── validateToken ─────────────────────────────────────────
    //  SE CACHEA — si el token ya fue validado antes no repite
    // la operación criptográfica (firma HS256)
    @Cacheable(value = "jwt-valid", key = "#token", unless = "#result == false")
    public boolean validateToken(String token)  {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            log.debug("Token validado y cacheado");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ── invalidateToken ───────────────────────────────────────
    //  Limpia el cache cuando el usuario cambia su password
    // o cuando se hace logout (futuro)
    @Override
    @CacheEvict(value = "jwt-valid", key = "#token")
    public void invalidateToken(String token) {
        log.info("Token removido del cache");
    }

    // ── obtenerEmailDesdeToken ────────────────────────────────
    @Override
    public String obtenerEmailDesdeToken(String token) {
        return getClaims(token).getSubject();
    }

    // ── obtenerUsuarioIdDesdeToken ────────────────────────────
    @Override
    public Integer obtenerUsuarioIdDesdeToken(String token) {
        return getClaims(token).get("usuarioId", Integer.class);
    }

    // ── obtenerAuthoritiesDesdeToken ──────────────────────────
    @Override
    public List<GrantedAuthority> obtenerAuthoritiesDesdeToken(String token) {
        String roles = getClaims(token).get("roles", String.class);

        // Si roles es null o vacío retorna lista vacía
        if (roles == null || roles.isBlank()) {
            return List.of();
        }

        return List.of(roles.split(",")).stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim()))
                .collect(Collectors.toList());
    }

    // ── helpers privados ──────────────────────────────────────
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
}