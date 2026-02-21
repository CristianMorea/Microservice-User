package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Config.JwtConfig;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;

    // ✅ Convierte el secret string a SecretKey


    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Usuario usuario) {

        String roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(usuario.getEmail())                    // ✅ setSubject() → subject()
                .claim("usuarioId", usuario.getUsuarioId())
                .claim("roles", roles)
                .issuedAt(now)                                  // ✅ setIssuedAt() → issuedAt()
                .expiration(exp)                                // ✅ setExpiration() → expiration()
                .signWith(getSigningKey())                      // ✅ ya no necesita el algoritmo explícito
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())                // ✅ setSigningKey() → verifyWith()
                    .build()
                    .parseSignedClaims(token);                 // ✅ parseClaimsJws() → parseSignedClaims()
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String obtenerEmailDesdeToken(String token) {
        return parsearClaims(token).getSubject();
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public List<GrantedAuthority> obtenerAuthoritiesDesdeToken(String token) {
        String roles = parsearClaims(token).get("roles", String.class);

        if (roles == null || roles.isBlank()) return List.of();

        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public Integer obtenerUsuarioIdDesdeToken(String token) {
        return parsearClaims(token).get("usuarioId", Integer.class); // ✅ Integer directo
    }
}