package com.ecomers.usuarios.TestUnitarios;


import com.ecomers.usuarios.Dto.*;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.JwtService;
import com.ecomers.usuarios.Service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ← No levanta Spring, solo Mockito
class UsuarioServiceTest {

    // ── MOCKS (dependencias falsas) ──────────────────────────
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private UsuarioRolRepository usuarioRolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    // ── CLASE A TESTEAR (recibe los mocks automáticamente) ───
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // ── DATOS REUTILIZABLES ──────────────────────────────────
    private UsuarioRegisterDTO dtoValido;
    private Usuario usuarioMock;
    private Rol rolClienteMock;

    @BeforeEach
        // Se ejecuta antes de cada @Test
    void setUp() {
        dtoValido = new UsuarioRegisterDTO();
        dtoValido.setEmail("test@test.com");
        dtoValido.setPasswordHash("Password123!");
        dtoValido.setNombre("Juan Test");
        dtoValido.setTelefono("123456789");
        dtoValido.setDireccion("Calle 123");

        usuarioMock = new Usuario();
        usuarioMock.setUsuarioId(1);
        usuarioMock.setEmail("test@test.com");
        usuarioMock.setPasswordHash("$2a$hashed");
        usuarioMock.setActive(true);

        rolClienteMock = new Rol();
        rolClienteMock.setId(1);
        rolClienteMock.setNombre("CLIENTE");
        rolClienteMock.setNivel(1);
    }


    // ════════════════════════════════════════════════════════
    //  registrarUsuario()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" Registro exitoso — crea usuario, perfil y asigna rol CLIENTE")
    void registrarUsuario_datosValidos_creaUsuarioPerfilYRol() {
        // ── ARRANGE (preparar) ───────────────────────────────
        // Le decimos al mock que el email NO existe aún
        when(usuarioRepository.existsByEmail("test@test.com"))
                .thenReturn(false);
        // El encoder devuelve un hash simulado
        when(passwordEncoder.encode("Password123!"))
                .thenReturn("$2a$hashed");
        // saveAndFlush devuelve el usuario mock
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenReturn(usuarioMock);
        // El rol CLIENTE existe en BD
        when(rolRepository.findByNombre("CLIENTE"))
                .thenReturn(Optional.of(rolClienteMock));

        // ── ACT (ejecutar) ───────────────────────────────────
        assertDoesNotThrow(() -> usuarioService.registrarUsuario(dtoValido));

        // ── ASSERT (verificar) ──────────────────────────────
        // ¿Se guardó el usuario?
        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
        // ¿Se creó el perfil?
        verify(perfilRepository).saveAndFlush(any(Perfil.class));
        // ¿Se asignó el rol?
        verify(usuarioRolRepository).save(any(UsuarioRol.class));
        // ¿Se codificó la contraseña?
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    @DisplayName(" Registro — la contraseña se guarda hasheada, no en texto plano")
    void registrarUsuario_passwordSeGuardaHasheada() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$hashed");
        when(usuarioRepository.saveAndFlush(any())).thenReturn(usuarioMock);
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rolClienteMock));

        usuarioService.registrarUsuario(dtoValido);

        // Capturamos el objeto Usuario que se pasó al save
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());

        Usuario guardado = captor.getValue();
        // La password guardada debe ser el hash, no el texto plano
        assertEquals("$2a$hashed", guardado.getPasswordHash());
        assertNotEquals("Password123!", guardado.getPasswordHash());
    }

    @Test
    @DisplayName(" Registro — el usuario se crea como activo por defecto")
    void registrarUsuario_usuarioSeCreaActivo() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(usuarioRepository.saveAndFlush(any())).thenReturn(usuarioMock);
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rolClienteMock));

        usuarioService.registrarUsuario(dtoValido);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());

        assertTrue(captor.getValue().isActive());
    }

    @Test
    @DisplayName(" Registro — lanza excepción si el email ya está registrado")
    void registrarUsuario_emailDuplicado_lanzaExcepcion() {
        // El email YA existe
        when(usuarioRepository.existsByEmail("test@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> usuarioService.registrarUsuario(dtoValido)
        );

        assertEquals("El email ya está registrado", ex.getMessage());
        // Si el email existe, NUNCA debe guardar nada
        verify(usuarioRepository, never()).saveAndFlush(any());
        verify(perfilRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName(" Registro — lanza excepción si el rol CLIENTE no existe en BD")
    void registrarUsuario_rolClienteNoExiste_lanzaExcepcion() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(usuarioRepository.saveAndFlush(any())).thenReturn(usuarioMock);
        // El rol CLIENTE NO existe
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> usuarioService.registrarUsuario(dtoValido));

        // Si no hay rol, tampoco debe asignar nada
        verify(usuarioRolRepository, never()).save(any());
    }


    // ════════════════════════════════════════════════════════
    //  login()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" Login exitoso — retorna DTO con token JWT")
    void login_credencialesValidas_retornaTokenJWT() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("Password123!");

        when(usuarioRepository.findActiveUserWithRoles("test@test.com"))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123!", "$2a$hashed"))
                .thenReturn(true);
        when(jwtService.generateToken(usuarioMock))
                .thenReturn("jwt-token-generado");

        LoginResponseDTO response = usuarioService.login(dto);

        assertNotNull(response);
        assertEquals("jwt-token-generado", response.getToken());
        assertEquals("test@test.com", response.getEmail());
        verify(jwtService).generateToken(usuarioMock);
    }

    @Test
    @DisplayName(" Login — lanza excepción si el usuario no existe")
    void login_usuarioNoExiste_lanzaExcepcion() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("noexiste@test.com");
        dto.setPassword("cualquier");

        when(usuarioRepository.findActiveUserWithRoles("noexiste@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.login(dto));

        assertEquals("Credenciales inválidas", ex.getMessage());
        // Si no existe usuario, nunca debe llegar a generar token
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName(" Login — lanza excepción si la contraseña es incorrecta")
    void login_passwordIncorrecta_lanzaExcepcion() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("wrongpassword");

        when(usuarioRepository.findActiveUserWithRoles("test@test.com"))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrongpassword", "$2a$hashed"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.login(dto));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    // ════════════════════════════════════════════════════════
    //  obtenerPerfil(), editarPerfil(), cambiarPassword()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" obtenerPerfil — retorna DTO del usuario")
    void obtenerPerfil_usuarioExiste_retornaDTO() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        PerfilResponseDTO result = usuarioService.obtenerPerfil(1);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName(" obtenerPerfil — lanza excepción si usuario no existe")
    void obtenerPerfil_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> usuarioService.obtenerPerfil(99));
    }

    @Test
    @DisplayName(" cambiarPassword — actualiza correctamente")
    void cambiarPassword_passwordActualCorrecta_actualiza() {
        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("Password123!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("NuevoPass456!");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123!", "$2a$hashed")).thenReturn(true);
        when(passwordEncoder.encode("NuevoPass456!")).thenReturn("$2a$newhashed");

        assertDoesNotThrow(() -> usuarioService.cambiarPassword(1, dto));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName(" cambiarPassword — lanza excepción si contraseñas nuevas no coinciden")
    void cambiarPassword_confirmacionNoCoincide_lanzaExcepcion() {
        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("Password123!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("OtroPass789!"); // ← no coincide

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123!", "$2a$hashed")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.cambiarPassword(1, dto));

        assertEquals("Las contraseñas nuevas no coinciden", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName(" cambiarPassword — lanza excepción si password actual es incorrecta")
    void cambiarPassword_passwordActualIncorrecta_lanzaExcepcion() {
        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("PasswordWrong!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("NuevoPass456!");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("PasswordWrong!", "$2a$hashed")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.cambiarPassword(1, dto));

        assertEquals("La contraseña actual es incorrecta", ex.getMessage());
    }

    @Test
    @DisplayName(" eliminar — hace soft delete, no borra de BD")
    void eliminar_usuarioExiste_marcaDeletedAt() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        usuarioService.eliminar(1);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario eliminado = captor.getValue();
        // Soft delete: tiene fecha de eliminación y está inactivo
        assertNotNull(eliminado.getDeletedAt());
        assertFalse(eliminado.isActive());
    }

    @Test
    @DisplayName(" obtenerTodos — excluye usuarios con deletedAt")
    void obtenerTodos_excluyeEliminados() {
        Usuario activo = new Usuario();
        activo.setEmail("activo@test.com");
        activo.setActive(true);

        Usuario eliminado = new Usuario();
        eliminado.setEmail("eliminado@test.com");
        eliminado.setDeletedAt(LocalDateTime.now());

        when(usuarioRepository.findAll()).thenReturn(List.of(activo, eliminado));

        List<PerfilResponseDTO> resultado = usuarioService.obtenerTodos();

        assertEquals(1, resultado.size());
        assertEquals("activo@test.com", resultado.get(0).getEmail());
    }





}
