package com.ecomers.usuarios.TestUnitarios;

import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Entitys.UsuarioRolId;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.TestUnitarios.impl.RolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    // ── Dependencias mockeadas ───────────────────────────────
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private UsuarioRolRepository usuarioRolRepository;

    // ── Clase real a testear ─────────────────────────────────
    @InjectMocks private RolServiceImpl rolService;

    // ── Datos reutilizables ──────────────────────────────────
    private Usuario usuarioMock;
    private Rol rolClienteMock;
    private Rol rolAdminMock;
    private UsuarioRol usuarioRolMock;

    @BeforeEach
    void setUp() {
        // Usuario base
        usuarioMock = new Usuario();
        usuarioMock.setUsuarioId(1);
        usuarioMock.setEmail("test@test.com");
        usuarioMock.setActive(true);
        usuarioMock.setRoles(new HashSet<>()); // Set mutable para poder modificar

        // Rol CLIENTE
        rolClienteMock = new Rol();
        rolClienteMock.setId(1);
        rolClienteMock.setNombre("CLIENTE");
        rolClienteMock.setNivel(1);

        // Rol ADMIN
        rolAdminMock = new Rol();
        rolAdminMock.setId(3);
        rolAdminMock.setNombre("ADMIN");
        rolAdminMock.setNivel(3);

        // Relación usuario-rol
        usuarioRolMock = new UsuarioRol();
        usuarioRolMock.setId(new UsuarioRolId());
        usuarioRolMock.setUsuario(usuarioMock);
        usuarioRolMock.setRol(rolClienteMock);
    }

    // ════════════════════════════════════════════════════════
    //  asignar()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" asignar — guarda el UsuarioRol correctamente")
    void asignar_datosValidos_guardaUsuarioRol() {
        // ARRANGE
        // El usuario existe
        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuarioMock));
        // El rol existe
        when(rolRepository.findByNombre("ADMIN"))
                .thenReturn(Optional.of(rolAdminMock));
        // El usuario NO tiene ese rol aún
        when(usuarioRolRepository.existsByUsuario_UsuarioIdAndRol_Nombre(1, "ADMIN"))
                .thenReturn(false);

        // ACT
        assertDoesNotThrow(() -> rolService.asignar(1, "ADMIN"));

        // ASSERT — se guardó la relación
        verify(usuarioRolRepository).save(any(UsuarioRol.class));
    }

    @Test
    @DisplayName(" asignar — el UsuarioRol guardado tiene el usuario y rol correctos")
    void asignar_datosValidos_usuarioRolTieneDatosCorrectos() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdminMock));
        when(usuarioRolRepository.existsByUsuario_UsuarioIdAndRol_Nombre(1, "ADMIN"))
                .thenReturn(false);

        rolService.asignar(1, "ADMIN");

        // Capturamos el objeto que se pasó al save para inspeccionarlo
        ArgumentCaptor<UsuarioRol> captor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(captor.capture());

        UsuarioRol guardado = captor.getValue();
        assertEquals(usuarioMock, guardado.getUsuario());
        assertEquals(rolAdminMock, guardado.getRol());
        assertNotNull(guardado.getAssignedAt()); // ← la fecha se asignó
    }

    @Test
    @DisplayName(" asignar — lanza excepción si el usuario no existe")
    void asignar_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.asignar(99, "ADMIN"));

        assertEquals("Usuario no encontrado", ex.getMessage());
        // Si no hay usuario, nunca debe buscar el rol ni guardar nada
        verify(rolRepository, never()).findByNombre(any());
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    @DisplayName(" asignar — lanza excepción si el rol no existe")
    void asignar_rolNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(rolRepository.findByNombre("SUPERADMIN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.asignar(1, "SUPERADMIN"));

        assertEquals("Rol 'SUPERADMIN' no existe", ex.getMessage());
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    @DisplayName(" asignar — lanza excepción si el usuario ya tiene ese rol")
    void asignar_rolYaAsignado_lanzaExcepcion() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rolClienteMock));
        // El usuario YA tiene CLIENTE
        when(usuarioRolRepository.existsByUsuario_UsuarioIdAndRol_Nombre(1, "CLIENTE"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.asignar(1, "CLIENTE"));

        assertEquals("El usuario ya tiene el rol CLIENTE", ex.getMessage());
        verify(usuarioRolRepository, never()).save(any());
    }

    // ════════════════════════════════════════════════════════
    //  quitar()
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName(" quitar — elimina el rol cuando el usuario tiene más de uno")
    void quitar_usuarioConDosRoles_eliminaCorrectamente() {
        // El usuario tiene CLIENTE y ADMIN — puede perder uno
        UsuarioRol urCliente = buildUsuarioRol(usuarioMock, rolClienteMock);
        UsuarioRol urAdmin   = buildUsuarioRol(usuarioMock, rolAdminMock);
        Set<UsuarioRol> roles = new HashSet<>(Set.of(urCliente, urAdmin));
        usuarioMock.setRoles(roles);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        // ACT — quitamos CLIENTE
        assertDoesNotThrow(() -> rolService.quitar(1, "CLIENTE"));

        // El usuario queda solo con ADMIN
        assertEquals(1, usuarioMock.getRoles().size());
        assertTrue(usuarioMock.getRoles().stream()
                .anyMatch(ur -> ur.getRol().getNombre().equals("ADMIN")));

        // JPA detecta el cambio y hace el DELETE via orphanRemoval
        verify(usuarioRepository).save(usuarioMock);
    }

    @Test
    @DisplayName(" quitar — lanza excepción si el usuario no existe")
    void quitar_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.quitar(99, "CLIENTE"));

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName(" quitar — lanza excepción si el usuario solo tiene un rol")
    void quitar_unicoRol_lanzaExcepcion() {
        // Solo tiene CLIENTE — no se puede quedar sin roles
        usuarioMock.setRoles(new HashSet<>(Set.of(usuarioRolMock)));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.quitar(1, "CLIENTE"));

        assertEquals("No puedes quitar el único rol del usuario", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName(" quitar — lanza excepción si el usuario no tiene ese rol")
    void quitar_rolQueNoTiene_lanzaExcepcion() {
        // Tiene CLIENTE y ADMIN, pero intentamos quitar VENDEDOR
        UsuarioRol urCliente = buildUsuarioRol(usuarioMock, rolClienteMock);
        UsuarioRol urAdmin   = buildUsuarioRol(usuarioMock, rolAdminMock);
        usuarioMock.setRoles(new HashSet<>(Set.of(urCliente, urAdmin)));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rolService.quitar(1, "VENDEDOR"));

        assertEquals("El usuario no tiene el rol VENDEDOR", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName(" quitar — el usuario guardado no contiene el rol eliminado")
    void quitar_verificaQueRolNoEstaEnUsuarioGuardado() {
        UsuarioRol urCliente = buildUsuarioRol(usuarioMock, rolClienteMock);
        UsuarioRol urAdmin   = buildUsuarioRol(usuarioMock, rolAdminMock);
        usuarioMock.setRoles(new HashSet<>(Set.of(urCliente, urAdmin)));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioMock));

        rolService.quitar(1, "ADMIN");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();
        // ADMIN ya no debe estar en los roles
        boolean tieneAdmin = guardado.getRoles().stream()
                .anyMatch(ur -> ur.getRol().getNombre().equals("ADMIN"));
        assertFalse(tieneAdmin);
    }

    // ── Helper para crear UsuarioRol en tests ────────────────
    private UsuarioRol buildUsuarioRol(Usuario usuario, Rol rol) {
        UsuarioRol ur = new UsuarioRol();
        ur.setId(new UsuarioRolId());
        ur.setUsuario(usuario);
        ur.setRol(rol);
        return ur;
    }
}