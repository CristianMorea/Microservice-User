package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Exeption.BadRequestException;
import com.ecomers.usuarios.Exeption.ConflictException;
import com.ecomers.usuarios.Exeption.NotFoundException;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.RolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolServiceImpl implements RolService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional
    public void asignar(Integer usuarioId, String rolNombre) {
        log.info("Asignando rol {} a usuario ID: {}", rolNombre, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado ID: {}", usuarioId);
                    return new NotFoundException("Usuario no encontrado");
                });

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> {
                    log.warn("Rol no encontrado: {}", rolNombre);
                    return new NotFoundException("Rol '" + rolNombre + "' no existe");
                });

        if (usuarioRolRepository.existsByUsuario_UsuarioIdAndRol_Nombre(usuarioId, rolNombre)) {
            log.warn("Usuario ID: {} ya tiene el rol {}", usuarioId, rolNombre);
            throw new ConflictException("El usuario ya tiene el rol " + rolNombre);
        }

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRol.setAssignedBy(usuario);
        usuarioRol.setAssignedAt(LocalDateTime.now());
        usuarioRolRepository.save(usuarioRol);

        log.info("Rol {} asignado exitosamente a usuario ID: {}", rolNombre, usuarioId);
    }

    @Override
    @Transactional
    public void quitar(Integer usuarioId, String rolNombre) {
        log.info("Quitando rol {} de usuario ID: {}", rolNombre, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado ID: {}", usuarioId);
                    return new NotFoundException("Usuario no encontrado");
                });

        if (usuario.getRoles().size() == 1) {
            log.warn("Intento de quitar único rol de usuario ID: {}", usuarioId);
            throw new BadRequestException("No puedes quitar el único rol del usuario");
        }

        boolean removido = usuario.getRoles().removeIf(
                ur -> ur.getRol().getNombre().equals(rolNombre));

        if (!removido) {
            log.warn("Usuario ID: {} no tiene el rol {}", usuarioId, rolNombre);
            throw new NotFoundException("El usuario no tiene el rol " + rolNombre);
        }

        usuarioRepository.save(usuario);
        log.info("Rol {} removido de usuario ID: {}", rolNombre, usuarioId);
    }
}