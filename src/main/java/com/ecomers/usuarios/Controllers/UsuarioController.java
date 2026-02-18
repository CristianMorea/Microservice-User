package com.ecomers.usuarios.Controllers;


import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Usuario;
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

}
