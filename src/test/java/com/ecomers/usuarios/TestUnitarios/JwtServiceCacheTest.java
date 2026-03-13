package com.ecomers.usuarios.TestUnitarios;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
//  Necesita contexto real de Spring para probar el cache
@SpringBootTest
@ActiveProfiles("test")
class JwtServiceCacheTest {

    @Autowired
    private JwtServiceImpl jwtService;

    @Autowired
    private CacheManager cacheManager;

    private String token;

    @BeforeEach
    void setUp() {
        // Crear usuario mock con rol
        Rol rol = new Rol();
        rol.setNombre("CLIENTE");

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setRol(rol);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(1);
        usuario.setEmail("test@test.com");
        usuario.setRoles(Set.of(usuarioRol));

        token = jwtService.generateToken(usuario);

        // Limpiar cache antes de cada test
        cacheManager.getCache("jwt-valid").clear();
    }

    @Test
    @DisplayName("✅ Token válido se cachea después de primera validación")
    void validateToken_seGuardaEnCache() {
        // No está en cache antes
        assertNull(cacheManager.getCache("jwt-valid").get(token));

        // Ejecutar validación
        boolean resultado = jwtService.validateToken(token);
        assertTrue(resultado);

        //  Verificar que el ValueWrapper no es null
        Cache.ValueWrapper cached = cacheManager.getCache("jwt-valid").get(token);
        assertNotNull(cached);
        assertEquals(true, cached.get());
    }

    @Test
    @DisplayName(" invalidateToken limpia el cache")
    void invalidateToken_limpiaCache() {
        // Poblar cache
        jwtService.validateToken(token);

        //  Verificar que está en cache
        Cache.ValueWrapper cached = cacheManager.getCache("jwt-valid").get(token);
        assertNotNull(cached);
        assertEquals(true, cached.get());

        // Invalidar
        jwtService.invalidateToken(token);

        //  Debe estar limpio
        assertNull(cacheManager.getCache("jwt-valid").get(token));
    }

    @Test
    @DisplayName(" Token inválido no se cachea")
    void validateToken_tokenInvalido_noSeGuardaEnCache() {
        String tokenFalso = "eyJ.falso.token";

        boolean resultado = jwtService.validateToken(tokenFalso);

        assertFalse(resultado);
        //  Token inválido no debe quedar en cache
        assertNull(cacheManager.getCache("jwt-valid").get(tokenFalso));
    }
}