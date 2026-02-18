package com.ecomers.usuarios.Service.impl;

import com.ecomers.usuarios.Dto.LoginResponseDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import com.ecomers.usuarios.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //Usuario ya implementa UserDetails, se retorna directamente
        return usuarioRepository.findActiveUserWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
