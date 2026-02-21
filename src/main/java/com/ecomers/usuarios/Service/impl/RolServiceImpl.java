package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.RolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final UsuarioRepository usuarioRepository;      // 👈 faltaba este
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional
    public void asignar(Integer usuarioId, String rolNombre) {
        Usuario usuario = usuarioRepository.findById(usuarioId)  // ✅
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new RuntimeException("Rol '" + rolNombre + "' no existe"));

        if (usuarioRolRepository.existsByUsuario_UsuarioIdAndRol_Nombre(usuarioId, rolNombre)) {
            throw new RuntimeException("El usuario ya tiene el rol " + rolNombre);
        }

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRol.setAssignedBy(usuario);
        usuarioRol.setAssignedAt(LocalDateTime.now());

        usuarioRolRepository.save(usuarioRol);
    }

    @Override
    @Transactional
    public void quitar(Integer usuarioId, String rolNombre) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRoles().size() == 1) {
            throw new RuntimeException("No puedes quitar el único rol del usuario");
        }

        // Elimina desde la colección del padre — orphanRemoval se encarga del DELETE
        boolean removido = usuario.getRoles().removeIf(
                ur -> ur.getRol().getNombre().equals(rolNombre)
        );

        if (!removido) {
            throw new RuntimeException("El usuario no tiene el rol " + rolNombre);
        }

        usuarioRepository.save(usuario); // JPA detecta el cambio y ejecuta el DELETE
    }
}
