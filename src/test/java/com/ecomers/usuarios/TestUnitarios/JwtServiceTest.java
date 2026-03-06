package com.ecomers.usuarios.TestUnitarios;

import com.ecomers.usuarios.Config.JwtConfig;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Entitys.UsuarioRolId;
import com.ecomers.usuarios.TestUnitarios.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtServiceImpl jwtService;

    // ── Constantes de test ───────────────────────────────────
    private static final String SECRET_VALIDO =
            "k7Hv2XpQmNjL9rTsWbYcAeUfGiOdZnCw1P4lVqBxKh8=";
    private static final long EXPIRACION_NORMAL = 3_600_000L;
    private static final long EXPIRACION_INMEDIATA = -1000L;

    // ── Datos reutilizables ──────────────────────────────────
    private Usuario usuarioConUnRol;
    private Usuario usuarioConDosRoles;
    private Usuario usuarioSinRoles;

    @BeforeEach
    void setUp() {
        //  FIX 1: lenient() evita UnnecessaryStubbingException en tests que
        // no generan token (validateToken con strings vacíos, nulos, aleatorios)
        lenient().when(jwtConfig.getSecret()).thenReturn(SECRET_VALIDO);
        lenient().when(jwtConfig.getExpiration()).thenReturn(EXPIRACION_NORMAL);

        usuarioConUnRol = buildUsuario(1, "cliente@test.com", "CLIENTE");
        usuarioConDosRoles = buildUsuarioConRoles(2, "admin@test.com", "CLIENTE", "ADMIN");

        usuarioSinRoles = new Usuario();
        usuarioSinRoles.setUsuarioId(3);
        usuarioSinRoles.setEmail("sinroles@test.com");
        usuarioSinRoles.setRoles(new HashSet<>());
    }

    // ════════════════════════════════════════════════════════
    //  generateToken()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("️ generateToken — retorna un token no nulo y no vacío")
    void generateToken_usuarioValido_retornaToken() {
        String token = jwtService.generateToken(usuarioConUnRol);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("️ generateToken — el token contiene el email como subject")
    void generateToken_contieneEmailComoSubject() {
        String token = jwtService.generateToken(usuarioConUnRol);

        String emailExtraido = jwtService.obtenerEmailDesdeToken(token);
        assertEquals("cliente@test.com", emailExtraido);
    }

    @Test
    @DisplayName("️ generateToken — el token contiene el usuarioId correcto")
    void generateToken_contieneUsuarioId() {
        String token = jwtService.generateToken(usuarioConUnRol);

        Integer idExtraido = jwtService.obtenerUsuarioIdDesdeToken(token);
        assertEquals(1, idExtraido);
    }

    @Test
    @DisplayName("️ generateToken — el token contiene los roles del usuario")
    void generateToken_contieneRoles() {
        String token = jwtService.generateToken(usuarioConUnRol);

        List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);

        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    @DisplayName("️ generateToken — usuario con dos roles contiene ambos en el token")
    void generateToken_dosRoles_ambosEnToken() {
        String token = jwtService.generateToken(usuarioConDosRoles);

        List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("️ generateToken — usuario sin roles genera token con roles vacíos")
    void generateToken_sinRoles_authoritiesVacias() {
        String token = jwtService.generateToken(usuarioSinRoles);

        List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);
        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("️ generateToken — dos tokens del mismo usuario son distintos (por timestamp)")
    void generateToken_dosLlamadas_tokensDistintos() throws InterruptedException {
        String token1 = jwtService.generateToken(usuarioConUnRol);

        // ✅ FIX 2: JWT usa iat en SEGUNDOS, no milisegundos.
        // Con 100ms ambos tokens caen en el mismo segundo y son idénticos.
        // Se necesita al menos 1000ms para que el iat cambie.
        Thread.sleep(1000);

        String token2 = jwtService.generateToken(usuarioConUnRol);

        assertNotEquals(token1, token2);
    }

    // ════════════════════════════════════════════════════════
    //  validateToken()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("️ validateToken — token válido retorna true")
    void validateToken_tokenValido_retornaTrue() {
        String token = jwtService.generateToken(usuarioConUnRol);

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("️ validateToken — token manipulado retorna false")
    void validateToken_tokenManipulado_retornaFalse() {
        String token = jwtService.generateToken(usuarioConUnRol);

        String[] partes = token.split("\\.");
        String tokenManipulado = partes[0] + ".payload_falso." + partes[2];

        assertFalse(jwtService.validateToken(tokenManipulado));
    }

    @Test
    @DisplayName("️ validateToken — token con firma incorrecta retorna false")
    void validateToken_firmaIncorrecta_retornaFalse() {
        when(jwtConfig.getSecret()).thenReturn(SECRET_VALIDO);
        String token = jwtService.generateToken(usuarioConUnRol);

        when(jwtConfig.getSecret()).thenReturn(
                "dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2Vj");

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    @DisplayName(" validateToken — token expirado retorna false")
    void validateToken_tokenExpirado_retornaFalse() {
        when(jwtConfig.getExpiration()).thenReturn(EXPIRACION_INMEDIATA);

        String tokenExpirado = jwtService.generateToken(usuarioConUnRol);

        assertFalse(jwtService.validateToken(tokenExpirado));
    }

    @Test
    @DisplayName("️ validateToken — string vacío retorna false")
    void validateToken_stringVacio_retornaFalse() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    @DisplayName("️ validateToken — string aleatorio retorna false")
    void validateToken_stringAleatorio_retornaFalse() {
        assertFalse(jwtService.validateToken("esto.no.es.un.jwt.valido"));
    }

    @Test
    @DisplayName("️ validateToken — null retorna false")
    void validateToken_null_retornaFalse() {
        assertFalse(jwtService.validateToken(null));
    }

    // ════════════════════════════════════════════════════════
    //  obtenerEmailDesdeToken()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" obtenerEmailDesdeToken — extrae el email correcto")
    void obtenerEmailDesdeToken_tokenValido_retornaEmail() {
        String token = jwtService.generateToken(usuarioConUnRol);

        String email = jwtService.obtenerEmailDesdeToken(token);

        assertEquals("cliente@test.com", email);
    }

    @Test
    @DisplayName(" obtenerEmailDesdeToken — funciona con distintos emails")
    void obtenerEmailDesdeToken_distintoEmail_retornaCorrecto() {
        String token = jwtService.generateToken(usuarioConDosRoles);

        String email = jwtService.obtenerEmailDesdeToken(token);

        assertEquals("admin@test.com", email);
    }

    // ════════════════════════════════════════════════════════
    //  obtenerUsuarioIdDesdeToken()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" obtenerUsuarioIdDesdeToken — extrae el ID correcto")
    void obtenerUsuarioIdDesdeToken_tokenValido_retornaId() {
        String token = jwtService.generateToken(usuarioConUnRol);

        Integer id = jwtService.obtenerUsuarioIdDesdeToken(token);

        assertEquals(1, id);
    }

    @Test
    @DisplayName(" obtenerUsuarioIdDesdeToken — IDs distintos en tokens distintos")
    void obtenerUsuarioIdDesdeToken_distintoUsuario_retornaIdCorrecto() {
        String token1 = jwtService.generateToken(usuarioConUnRol);
        String token2 = jwtService.generateToken(usuarioConDosRoles);

        assertEquals(1, jwtService.obtenerUsuarioIdDesdeToken(token1));
        assertEquals(2, jwtService.obtenerUsuarioIdDesdeToken(token2));
    }

    // ════════════════════════════════════════════════════════
    //  obtenerAuthoritiesDesdeToken()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" obtenerAuthoritiesDesdeToken — agrega prefijo ROLE_ a cada rol")
    void obtenerAuthoritiesDesdeToken_agregaPrefijoROLE() {
        String token = jwtService.generateToken(usuarioConUnRol);

        List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);

        assertTrue(authorities.stream()
                .allMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }

    @Test
    @DisplayName(" obtenerAuthoritiesDesdeToken — lista vacía si no hay roles")
    void obtenerAuthoritiesDesdeToken_sinRoles_listaVacia() {
        String token = jwtService.generateToken(usuarioSinRoles);

        List<GrantedAuthority> authorities = jwtService.obtenerAuthoritiesDesdeToken(token);

        assertTrue(authorities.isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────

    private Usuario buildUsuario(Integer id, String email, String... nombreRoles) {
        Usuario usuario = new Usuario();
        usuario.setUsuarioId(id);
        usuario.setEmail(email);
        usuario.setActive(true);
        usuario.setRoles(buildRoles(usuario, nombreRoles));
        return usuario;
    }

    private Usuario buildUsuarioConRoles(Integer id, String email, String... nombreRoles) {
        return buildUsuario(id, email, nombreRoles);
    }

    private Set<UsuarioRol> buildRoles(Usuario usuario, String... nombreRoles) {
        Set<UsuarioRol> roles = new HashSet<>();
        int rolId = 1;
        for (String nombre : nombreRoles) {
            Rol rol = new Rol();
            rol.setId(rolId++);
            rol.setNombre(nombre);

            UsuarioRol ur = new UsuarioRol();
            ur.setId(new UsuarioRolId());
            ur.setUsuario(usuario);
            ur.setRol(rol);
            roles.add(ur);
        }
        return roles;
    }
}