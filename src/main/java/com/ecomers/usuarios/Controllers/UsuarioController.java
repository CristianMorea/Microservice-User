package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.TestUnitarios.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UsuarioRegisterDTO dto) {
        Usuario guardado = usuarioService.registrarUsuario(dto);

        // ✅ Retorna un JSON con email y nombre, SIN passwordHash
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "email", guardado.getEmail(),
                "nombre", dto.getNombre()   // viene del DTO porque Usuario no tiene nombre
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }
}