package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Dto.*;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.JwtService;
import com.ecomers.usuarios.Service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        Usuario UsuarioGuardado = usuarioRepository.saveAndFlush(usuario);
        // 3.  CREAR PERFIL

        Perfil perfil = new Perfil();
        perfil.setUsuario(UsuarioGuardado);
        perfil.setNombre(dto.getNombre());
        perfil.setDireccion(dto.getDireccion());
        perfil.setTelefono(dto.getTelefono());
        perfil.setAvatarUrl(dto.getAvatarUrl());
        perfil.setUpdated_at(LocalDateTime.now());

        perfilRepository.saveAndFlush(perfil);

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

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {

        // 1. Buscar usuario activo
        Usuario usuario = usuarioRepository
                .findActiveUserWithRoles(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Validar contraseña
        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 3. Obtener nombre desde perfil
        String nombre = usuario.getPerfil() != null
                ? usuario.getPerfil().getNombre()
                : "";

        // 4. Obtener rol principal
        String rol = usuario.getRoles().stream()
                .findFirst()
                .map(ur -> ur.getRol().getNombre())
                .orElse("SIN ROL");

        // 5. ✅ Generar token JWT aquí

        String token = jwtService.generateToken(usuario);

        return LoginResponseDTO.builder()
                .usuarioId(usuario.getUsuarioId())
                .email(usuario.getEmail())
                .nombre(nombre)
                .rol(rol)
                .token(token) // ✅ Token incluido en la respuesta
                .build();
    }


    private PerfilResponseDTO toPerfilDTO(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toList());

        Perfil perfil = usuario.getPerfil();

        // ✅ Builder en lugar de new PerfilResponseDTO(...)
        return PerfilResponseDTO.builder()
                .usuarioId(usuario.getUsuarioId())
                .nombre(perfil != null ? perfil.getNombre() : "")
                .email(usuario.getEmail())
                .telefono(perfil != null ? perfil.getTelefono() : null)
                .direccion(perfil != null ? perfil.getDireccion() : null)
                .avatarUrl(perfil != null ? perfil.getAvatarUrl() : null)
                .rol(roles)
                .build();
    }

    // ✅ Ver perfil propio
    @Override
    public PerfilResponseDTO obtenerPerfil(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toPerfilDTO(usuario);
    }

    // ✅ Editar perfil propio
    @Override
    @Transactional
    public PerfilResponseDTO editarPerfil(Integer usuarioId, EditarPerfilDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            perfil.setUsuario(usuario);
        }

        perfil.setNombre(dto.getNombre());
        perfil.setTelefono(dto.getTelefono());
        perfil.setDireccion(dto.getDireccion());
        perfil.setUpdated_at(LocalDateTime.now());

        perfilRepository.save(perfil);
        return toPerfilDTO(usuario);
    }

    // ✅ Cambiar contraseña
    @Override
    @Transactional
    public void cambiarPassword(Integer usuarioId, CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verifica que la contraseña actual sea correcta
        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Verifica que la nueva coincida con la confirmación
        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }

        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    // ✅ Listar todos los usuarios (ADMIN)
    @Override
    public List<PerfilResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null) // excluye eliminados
                .map(this::toPerfilDTO)
                .collect(Collectors.toList());
    }

    // ✅ Soft delete — no elimina de la BD, solo marca deletedAt
    @Override
    @Transactional
    public void eliminar(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setDeletedAt(LocalDateTime.now());
        usuario.setActive(false);
        usuarioRepository.save(usuario);
    }

}
