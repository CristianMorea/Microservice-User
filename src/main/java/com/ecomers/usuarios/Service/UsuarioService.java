package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Usuario;

public interface UsuarioService {

    Usuario registrarUsuario(UsuarioRegisterDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);


}
