package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.CambiarPasswordDTO;
import com.ecomers.usuarios.Dto.EditarPerfilDTO;
import com.ecomers.usuarios.Dto.PerfilResponseDTO;
import com.ecomers.usuarios.Service.JwtService;
import com.ecomers.usuarios.Service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/Perfil")
@RequiredArgsConstructor

public class PerfilController  {


    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    // Ver su propio perfil
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilResponseDTO> verPerfil(HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        return ResponseEntity.ok(usuarioService.obtenerPerfil(usuarioId));
    }

    //Editar su propio perfil
    @PutMapping("/editar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerfilResponseDTO> editarPerfil(@RequestBody EditarPerfilDTO dto,
                                                          HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        return ResponseEntity.ok(usuarioService.editarPerfil(usuarioId, dto));
    }

    //Cambiar contraseña
    @PutMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cambiarPassword(@RequestBody CambiarPasswordDTO dto,
                                             HttpServletRequest request) {
        Integer usuarioId = jwtService.obtenerUsuarioIdDesdeToken(extraerToken(request));
        usuarioService.cambiarPassword(usuarioId, dto);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    private String extraerToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}
