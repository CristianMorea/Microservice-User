package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Dto.*;
import com.ecomers.usuarios.Entitys.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface UsuarioService {

    Usuario registrarUsuario(UsuarioRegisterDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);

    PerfilResponseDTO obtenerPerfil(Integer usuarioId);
    PerfilResponseDTO editarPerfil(Integer usuarioId, EditarPerfilDTO dto);
    void cambiarPassword(Integer usuarioId, CambiarPasswordDTO dto);
    List<PerfilResponseDTO> obtenerTodos();
    void eliminar(Integer usuarioId);

}
