package com.ecomers.usuarios.Integration;

import com.ecomers.usuarios.Dto.AsignarRolDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest extends BaseIntegrationTest {


    //  GET /admin/listar
    @Test
    @DisplayName(" GET /admin/listar — 200 solo con rol ADMIN")
    void listar_conRolAdmin_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        crearUsuario("cliente1@test.com", "Pass123!", rolCliente);
        crearUsuario("cliente2@test.com", "Pass123!", rolCliente);

        mockMvc.perform(get("/admin/listar")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios.length()").value(3));
    }

    @Test
    @DisplayName(" GET /admin/listar — 403 con rol CLIENTE")
    void listar_conRolCliente_retorna403() throws Exception {
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        mockMvc.perform(get("/admin/listar")
                        .header("Authorization", bearerToken(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName(" GET /admin/listar — 401 sin JWT")
    void listar_sinJWT_retorna401() throws Exception {
        mockMvc.perform(get("/admin/listar"))
                .andExpect(status().isUnauthorized());
    }

    //  GET /admin/{id}


    @Test
    @DisplayName(" GET /admin/{id} — 200 retorna perfil del usuario")
    void verUsuario_conRolAdmin_retorna200() throws Exception {
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        mockMvc.perform(get("/admin/" + cliente.getUsuarioId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@test.com"));
    }

    @Test
    @DisplayName(" GET /admin/{id} — 404 si usuario no existe")
    void verUsuario_idInexistente_retorna404() throws Exception {
        Usuario admin = crearUsuario("admin@test.com", "Pass123!", rolAdmin);

        mockMvc.perform(get("/admin/9999")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName(" GET /admin/{id} — 403 si lo intenta un CLIENTE")
    void verUsuario_conRolCliente_retorna403() throws Exception {
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        mockMvc.perform(get("/admin/" + cliente.getUsuarioId())
                        .header("Authorization", bearerToken(cliente)))
                .andExpect(status().isForbidden());
    }


    //  DELETE /admin/{id}


    @Test
    @DisplayName(" DELETE /admin/{id} — 204 soft delete exitoso")
    void eliminar_conRolAdmin_retorna204() throws Exception {
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        mockMvc.perform(delete("/admin/" + cliente.getUsuarioId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName(" DELETE /admin/{id} — usuario eliminado no aparece en listar")
    void eliminar_usuarioEliminadoNoApareceEnListado() throws Exception {
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("eliminado@test.com", "Pass123!", rolCliente);

        // Eliminamos
        mockMvc.perform(delete("/admin/" + cliente.getUsuarioId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNoContent());

        // Verificamos que no aparece en el listado
        mockMvc.perform(get("/admin/listar")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'eliminado@test.com')]").isEmpty());
    }


    //  POST /admin/{id}/roles


    @Test
    @DisplayName(" POST /admin/{id}/roles — asigna rol correctamente")
    void asignarRol_conRolAdmin_retorna200() throws Exception {
        crearRolSiNoExiste("VENDEDOR", 2); // nos aseguramos que exista
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setRolNombre("VENDEDOR");

        mockMvc.perform(post("/admin/" + cliente.getUsuarioId() + "/roles")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName(" POST /admin/{id}/roles — 409 si el usuario ya tiene ese rol")
    void asignarRol_rolDuplicado_retorna409() throws Exception {
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setRolNombre("CLIENTE"); // ya lo tiene

        mockMvc.perform(post("/admin/" + cliente.getUsuarioId() + "/roles")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El usuario ya tiene el rol CLIENTE"));
    }

    // ════════════════════════════════════════════════════════
    //  DELETE /admin/{id}/roles/{rolNombre}
    // ════════════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /admin/{id}/roles/{rol} — quita rol correctamente")
    void quitarRol_conDosRoles_retorna204() throws Exception {

        crearRolSiNoExiste("VENDEDOR", 2);

        Usuario admin = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        // Asignar segundo rol (VENDEDOR)
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setRolNombre("VENDEDOR");

        mockMvc.perform(post("/admin/" + cliente.getUsuarioId() + "/roles")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Quitar rol CLIENTE (debe quedar solo VENDEDOR)
        mockMvc.perform(delete("/admin/" + cliente.getUsuarioId() + "/roles/CLIENTE")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName(" DELETE /admin/{id}/roles/{rol} — 400 si es el único rol")
    void quitarRol_unicoRol_retorna400() throws Exception {
        Usuario admin   = crearUsuario("admin@test.com", "Pass123!", rolAdmin);
        Usuario cliente = crearUsuario("cliente@test.com", "Pass123!", rolCliente);

        mockMvc.perform(delete("/admin/" + cliente.getUsuarioId() + "/roles/CLIENTE")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("No puedes quitar el único rol del usuario"));
    }
}