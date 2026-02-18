package com.ecomers.usuarios.Service;

import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Usuario;

public interface UsuarioService {

    Usuario registrarUsuario(UsuarioRegisterDTO dto);

}
