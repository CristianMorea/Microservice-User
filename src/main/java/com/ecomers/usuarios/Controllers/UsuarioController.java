package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro y login de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario con rol CLIENTE asignado automáticamente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Email duplicado o datos inválidos")
    })
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UsuarioRegisterDTO dto) {
        Usuario guardado = usuarioService.registrarUsuario(dto);

        // ✅ Retorna un JSON con email y nombre, SIN passwordHash
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "email", guardado.getEmail(),
                "nombre", dto.getNombre()   // viene del DTO porque Usuario no tiene nombre
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Retorna JWT para usar en endpoints protegidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso con token JWT"),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }
}