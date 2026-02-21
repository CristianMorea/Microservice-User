package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.AsignarRolDTO;
import com.ecomers.usuarios.Dto.PerfilResponseDTO;
import com.ecomers.usuarios.Service.RolService;
import com.ecomers.usuarios.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor

public class AdminUsuarioController  {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    //  Listar todos los usuarios
    @GetMapping("listar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PerfilResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    //  Ver el perfil de cualquier usuario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerfilResponseDTO> verUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    //  Eliminar usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    //  Asignar rol
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarRol(@PathVariable Integer id,
                                        @RequestBody AsignarRolDTO dto) {
        rolService.asignar(id, dto.getRolNombre()); // ✅
        return ResponseEntity.ok("Rol asignado correctamente");
    }

    //  Quitar rol
    @DeleteMapping("/{id}/roles/{rolNombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> quitarRol(@PathVariable Integer id,
                                       @PathVariable String rolNombre) {
        rolService.quitar(id, rolNombre);
        return ResponseEntity.noContent().build();
    }
}
