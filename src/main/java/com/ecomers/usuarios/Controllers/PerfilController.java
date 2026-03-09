package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.CambiarPasswordDTO;
import com.ecomers.usuarios.Dto.EditarPerfilDTO;
import com.ecomers.usuarios.Dto.PerfilResponseDTO;
import com.ecomers.usuarios.Service.JwtService;
import com.ecomers.usuarios.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/Perfil")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gestión del perfil del usuario autenticado")
@SecurityRequirement(name = "Bearer Authentication")

public class PerfilController  {


    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    // Ver su propio perfil
    @GetMapping
    @Operation(summary = "Ver mi perfil", description = "Retorna el perfil del usuario autenticado via JWT")
    @ApiResponse(responseCode = "200", description = "Perfil encontrado")
    @ApiResponse(responseCode = "401", description = "Token inválido o ausente")

    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilResponseDTO> verPerfil(HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        return ResponseEntity.ok(usuarioService.obtenerPerfil(usuarioId));
    }

    //Editar su propio perfil
    @PutMapping("/editar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Editar mi perfil")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<PerfilResponseDTO> editarPerfil(@Valid @RequestBody EditarPerfilDTO dto,
                                                          HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        return ResponseEntity.ok(usuarioService.editarPerfil(usuarioId, dto));
    }

    //Cambiar contraseña
    @PutMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cambiar contraseña")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o confirmación no coincide")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<?> cambiarPassword(@Valid @RequestBody CambiarPasswordDTO dto,
                                             HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        usuarioService.cambiarPassword(usuarioId, dto);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    private String extraerToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}
