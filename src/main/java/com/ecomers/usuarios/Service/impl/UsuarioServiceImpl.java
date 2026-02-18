package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public Usuario registrarUsuario(UsuarioRegisterDTO dto) {

        //1. Validar Email unico
        if(usuarioRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("El email ya está registrado");
        }

        // 2. CREAR USUARIO

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setActive(true);

        Usuario UsuarioGuardado = usuarioRepository.save(usuario);

        // 3.  CREAR PERFIL

        Perfil perfil = new Perfil();
        perfil.setUsuario(UsuarioGuardado);
        perfil.setNombre(dto.getNombre());
        perfil.setDireccion(dto.getDireccion());
        perfil.setTelefono(dto.getTelefono());
        perfil.setAvatarUrl(dto.getAvatarUrl());
        perfil.setUpdated_at(LocalDateTime.now());

        perfilRepository.save(perfil);

        // 4 ASIGNAR ROL CLIENTE

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no existe"));

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(UsuarioGuardado);
        usuarioRol.setRol(rolCliente);
        usuarioRol.setAssignedBy(UsuarioGuardado);
        usuarioRol.setAssignedAt(LocalDateTime.now());

        usuarioRolRepository.save(usuarioRol);

        return UsuarioGuardado;

    }
}
