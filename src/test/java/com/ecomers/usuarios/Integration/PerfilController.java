package com.ecomers.usuarios.Integration;

import com.ecomers.usuarios.Dto.CambiarPasswordDTO;
import com.ecomers.usuarios.Dto.EditarPerfilDTO;
import com.ecomers.usuarios.Entitys.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PerfilControllerTest extends BaseIntegrationTest {

    //
    //  GET /Perfil
    //

    @Test
    @DisplayName(" GET /Perfil — 200 con JWT válido")
    void verPerfil_conJWT_retorna200() throws Exception {
        Usuario usuario = crearUsuario("perfil@test.com", "Pass123!", rolCliente);

        mockMvc.perform(get("/Perfil")
                        .header("Authorization", bearerToken(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("perfil@test.com"))
                .andDo(print());
    }

    @Test
    @DisplayName(" GET /Perfil — 401 sin JWT")
    void verPerfil_sinJWT_retorna401() throws Exception {
        mockMvc.perform(get("/Perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName(" GET /Perfil — 401 con token inválido")
    void verPerfil_tokenInvalido_retorna401() throws Exception {
        mockMvc.perform(get("/Perfil")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName(" GET /Perfil — retorna los datos del usuario autenticado, no de otro")
    void verPerfil_retornaDatosDelUsuarioAutenticado() throws Exception {
        Usuario usuarioA = crearUsuario("usuarioA@test.com", "Pass123!", rolCliente);
        crearUsuario("usuarioB@test.com", "Pass123!", rolCliente);

        // Hacemos request con token de A
        mockMvc.perform(get("/Perfil")
                        .header("Authorization", bearerToken(usuarioA)))
                .andExpect(status().isOk())
                // La respuesta contiene datos de A, no de B
                .andExpect(jsonPath("$.email").value("usuarioA@test.com"));
    }

    //
    //  PUT /Perfil/editar


    @Test
    @DisplayName("PUT /Perfil/editar — 200 actualiza los datos")
    void editarPerfil_conJWT_actualiza() throws Exception {
        Usuario usuario = crearUsuario("editar@test.com", "Pass123!", rolCliente);

        EditarPerfilDTO dto = new EditarPerfilDTO();
        dto.setNombre("Nombre Actualizado");
        dto.setTelefono("999888777");
        dto.setDireccion("Nueva Dirección 456");

        mockMvc.perform(put("/Perfil/editar")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.telefono").value("999888777"))
                .andDo(print());
    }

    @Test
    @DisplayName(" PUT /Perfil/editar — 401 sin JWT")
    void editarPerfil_sinJWT_retorna401() throws Exception {
        EditarPerfilDTO dto = new EditarPerfilDTO();
        dto.setNombre("Nombre");

        mockMvc.perform(put("/Perfil/editar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


    //  PUT /Perfil/cambiar-password


    @Test
    @DisplayName(" PUT /Perfil/cambiar-password — 200 con datos correctos")
    void cambiarPassword_datosCorrectos_retorna200() throws Exception {
        Usuario usuario = crearUsuario("cambiopass@test.com", "Pass123!", rolCliente);

        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("Pass123!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("NuevoPass456!");

        mockMvc.perform(put("/Perfil/cambiar-password")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName(" PUT /Perfil/cambiar-password — 400 si contraseña actual incorrecta")
    void cambiarPassword_passwordActualIncorrecta_retorna400() throws Exception {
        Usuario usuario = crearUsuario("cambiopass@test.com", "Pass123!", rolCliente);

        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("WrongPass!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("NuevoPass456!");

        mockMvc.perform(put("/Perfil/cambiar-password")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La contraseña actual es incorrecta"));
    }

    @Test
    @DisplayName(" PUT /Perfil/cambiar-password — 400 si confirmación no coincide")
    void cambiarPassword_confirmacionNoCoincide_retorna400() throws Exception {
        Usuario usuario = crearUsuario("cambiopass@test.com", "Pass123!", rolCliente);

        CambiarPasswordDTO dto = new CambiarPasswordDTO();
        dto.setPasswordActual("Pass123!");
        dto.setPasswordNueva("NuevoPass456!");
        dto.setConfirmarPassword("OtroPass789!"); // ← no coincide

        mockMvc.perform(put("/Perfil/cambiar-password")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Las contraseñas nuevas no coinciden"));
    }

    @Test
    @DisplayName(" PUT /Perfil/cambiar-password — 401 sin JWT")
    void cambiarPassword_sinJWT_retorna401() throws Exception {
        mockMvc.perform(put("/Perfil/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}