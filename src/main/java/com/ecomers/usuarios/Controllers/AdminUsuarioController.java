package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.AsignarRolDTO;
import com.ecomers.usuarios.Dto.PerfilResponseDTO;
import com.ecomers.usuarios.Service.RolService;
import com.ecomers.usuarios.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Operaciones administrativas — requiere rol ADMIN")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUsuarioController  {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    //  Listar todos los usuarios
    @GetMapping("listar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "403", description = "No tienes rol ADMIN")
    public ResponseEntity<List<PerfilResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    //  Ver el perfil de cualquier usuario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver perfil de cualquier usuario")
    @ApiResponse(responseCode = "200", description = "Perfil encontrado")
    @ApiResponse(responseCode = "400", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "403", description = "No tienes rol ADMIN")
    public ResponseEntity<PerfilResponseDTO> verUsuario(@Valid @PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    //  Eliminar usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")

    @Operation(summary = "Eliminar usuario", description = "Soft delete — marca deletedAt, no borra de BD")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado")
    @ApiResponse(responseCode = "403", description = "No tienes rol ADMIN")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    //  Asignar rol
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar rol a usuario")
    @ApiResponse(responseCode = "200", description = "Rol asignado")
    @ApiResponse(responseCode = "400", description = "Rol no existe o usuario ya lo tiene")

    public ResponseEntity<?> asignarRol(@Valid @PathVariable Integer id,
                                        @Valid@RequestBody AsignarRolDTO dto) {
        rolService.asignar(id, dto.getRolNombre()); // ✅
        return ResponseEntity.ok("Rol asignado correctamente");
    }

    //  Quitar rol
    @DeleteMapping("/{id}/roles/{rolNombre}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Quitar rol a usuario")
    @ApiResponse(responseCode = "204", description = "Rol removido")
    @ApiResponse(responseCode = "400", description = "Usuario no tiene ese rol o es su único rol")

    public ResponseEntity<?> quitarRol(@Valid @PathVariable Integer id,
                                       @Valid@PathVariable String rolNombre) {
        rolService.quitar(id, rolNombre);
        return ResponseEntity.noContent().build();
    }
}
