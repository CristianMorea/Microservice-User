package com.ecomers.usuarios.TestUnitarios;


public interface RolService {
    void asignar(Integer usuarioId, String rolNombre);


    void quitar(Integer usuarioId, String rolNombre);
}
