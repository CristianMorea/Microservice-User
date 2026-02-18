package com.ecomers.usuarios.Controllers;
import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor

public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")

    public ResponseEntity<?> register(@RequestBody UsuarioRegisterDTO dto){
        usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto){

        LoginResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }

}
