package com.ecomers.usuarios.Controllers;

import com.ecomers.usuarios.Config.LoginRateLimiter;
import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterResponseDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Registro y autenticación")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public ResponseEntity<UsuarioRegisterResponseDTO> register(
            @Valid @RequestBody UsuarioRegisterDTO dto) {

        Usuario usuario = usuarioService.registrarUsuario(dto);

        UsuarioRegisterResponseDTO response = UsuarioRegisterResponseDTO.builder()
                .usuarioId(usuario.getUsuarioId())
                .email(usuario.getEmail())
                .nombre(dto.getNombre())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión — retorna JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos — espera 1 minuto")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDTO dto,
            HttpServletRequest request) {

        // Obtener IP real (considera proxies)
        String ip = obtenerIp(request);

        // Verificar rate limit
        if (!loginRateLimiter.intentoPermitido(ip)) {
            log.warn("Rate limit alcanzado para IP: {}", ip);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Demasiados intentos de login. Espera 1 minuto.");
        }

        LoginResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }

    // Obtiene IP real considerando proxies y load balancers
    private String obtenerIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For puede tener múltiples IPs — tomar la primera
        return ip.split(",")[0].trim();
    }
}