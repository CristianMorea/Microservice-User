package com.ecomers.usuarios.Service;


public interface RolService {
    void asignar(Integer usuarioId, String rolNombre);


    void quitar(Integer usuarioId, String rolNombre);
}
