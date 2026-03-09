package com.ecomers.usuarios.Service.impl;



import com.ecomers.usuarios.Dto.*;
import com.ecomers.usuarios.Entitys.Perfil;
import com.ecomers.usuarios.Entitys.Rol;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Entitys.UsuarioRol;
import com.ecomers.usuarios.Exeption.BadRequestException;
import com.ecomers.usuarios.Exeption.ConflictException;
import com.ecomers.usuarios.Exeption.NotFoundException;
import com.ecomers.usuarios.Repository.PerfilRepository;
import com.ecomers.usuarios.Repository.RolRepository;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import com.ecomers.usuarios.Repository.UsuarioRolRepository;
import com.ecomers.usuarios.Service.JwtService;
import com.ecomers.usuarios.Service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
        log.info("Intentando registrar usuario: {}", dto.getEmail());

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registro fallido — email duplicado: {}", dto.getEmail());
            throw new ConflictException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setActive(true);

        Usuario usuarioGuardado = usuarioRepository.saveAndFlush(usuario);
        log.info("Usuario creado con ID: {}", usuarioGuardado.getUsuarioId());

        Perfil perfil = new Perfil();
        perfil.setUsuario(usuarioGuardado);
        perfil.setNombre(dto.getNombre());
        perfil.setDireccion(dto.getDireccion());
        perfil.setTelefono(dto.getTelefono());
        perfil.setAvatarUrl(dto.getAvatarUrl());
        perfil.setUpdated_at(LocalDateTime.now());
        perfilRepository.saveAndFlush(perfil);
        log.info("Perfil creado para usuario ID: {}", usuarioGuardado.getUsuarioId());

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> {
                    log.error("Rol CLIENTE no encontrado — verifica DataInitializer");
                    return new NotFoundException("Rol CLIENTE no existe");
                });

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuarioGuardado);
        usuarioRol.setRol(rolCliente);
        usuarioRol.setAssignedBy(usuarioGuardado);
        usuarioRol.setAssignedAt(LocalDateTime.now());
        usuarioRolRepository.save(usuarioRol);
        log.info("Rol CLIENTE asignado a usuario ID: {}", usuarioGuardado.getUsuarioId());

        return usuarioGuardado;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        log.info("Intento de login: {}", dto.getEmail());

        Usuario usuario = usuarioRepository
                .findActiveUserWithRoles(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login fallido — usuario no encontrado: {}", dto.getEmail());
                    return new BadRequestException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            log.warn("Login fallido — password incorrecta para: {}", dto.getEmail());
            throw new BadRequestException("Credenciales inválidas");
        }

        String nombre = usuario.getPerfil() != null ? usuario.getPerfil().getNombre() : "";
        String rol = usuario.getRoles().stream()
                .findFirst()
                .map(ur -> ur.getRol().getNombre())
                .orElse("SIN ROL");

        String token = jwtService.generateToken(usuario);
        log.info("Login exitoso para usuario ID: {}", usuario.getUsuarioId());

        return LoginResponseDTO.builder()
                .usuarioId(usuario.getUsuarioId())
                .email(usuario.getEmail())
                .nombre(nombre)
                .rol(rol)
                .token(token)
                .build();
    }

    private PerfilResponseDTO toPerfilDTO(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .collect(Collectors.toList());

        Perfil perfil = usuario.getPerfil();

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

    @Override
    @Transactional
    public PerfilResponseDTO obtenerPerfil(Integer usuarioId) {
        log.info("Obteniendo perfil de usuario ID: {}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado ID: {}", usuarioId);
                    return new NotFoundException("Usuario no encontrado");
                });
        return toPerfilDTO(usuario);
    }

    @Override
    @Transactional
    public PerfilResponseDTO editarPerfil(Integer usuarioId, EditarPerfilDTO dto) {
        log.info("Editando perfil de usuario ID: {}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            perfil.setUsuario(usuario);
        }

        perfil.setNombre(dto.getNombre());
        perfil.setTelefono(dto.getTelefono());
        perfil.setDireccion(dto.getDireccion());
        perfil.setUpdated_at(LocalDateTime.now());
        perfilRepository.saveAndFlush(perfil);

        Usuario actualizado = usuarioRepository.findById(usuarioId).orElseThrow();
        log.info("Perfil actualizado para usuario ID: {}", usuarioId);
        return toPerfilDTO(actualizado);
    }

    @Override
    @Transactional
    public void cambiarPassword(Integer usuarioId, CambiarPasswordDTO dto) {
        log.info("Cambiando password de usuario ID: {}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPasswordHash())) {
            log.warn("Password actual incorrecta para usuario ID: {}", usuarioId);
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }

        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepository.save(usuario);
        log.info("Password actualizada para usuario ID: {}", usuarioId);
    }

    @Override
    @Transactional
    public List<PerfilResponseDTO> obtenerTodos() {
        log.info("Listando todos los usuarios activos");
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::toPerfilDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Integer usuarioId) {
        log.info("Eliminando usuario ID: {}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminar usuario inexistente ID: {}", usuarioId);
                    return new NotFoundException("Usuario no encontrado");
                });

        usuario.setDeletedAt(LocalDateTime.now());
        usuario.setActive(false);
        usuarioRepository.save(usuario);
        log.info("Usuario ID: {} eliminado (soft delete)", usuarioId);
    }
}