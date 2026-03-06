package com.ecomers.usuarios.Integration;



import com.ecomers.usuarios.Dto.LoginRequestDTO;
import com.ecomers.usuarios.Dto.UsuarioRegisterDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest extends BaseIntegrationTest {

    //
    //  POST /usuarios/register
    //

    @Test
    @DisplayName(" register — 201 con datos válidos")
    void register_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO("nuevo@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.nombre").value("Juan Test"))
                .andDo(print());
    }

    @Test
    @DisplayName("register — la respuesta no contiene la contraseña")
    void register_respuestaNoCotienePassword() throws Exception {
        mockMvc.perform(post("/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO("nuevo@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName(" register — 400 con email duplicado")
    void register_emailDuplicado_retorna400() throws Exception {
        // Primero creamos el usuario
        crearUsuario("duplicado@test.com", "Pass123!", rolCliente);

        // Intentamos registrar con el mismo email
        mockMvc.perform(post("/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO("duplicado@test.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }

    @Test
    @DisplayName(" register — 400 con email inválido")
    void register_emailInvalido_retorna400() throws Exception {
        UsuarioRegisterDTO dto = buildRegisterDTO("no-es-un-email");
        dto.setEmail("no-es-un-email");

        mockMvc.perform(post("/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists()); // error de validación en campo email
    }

    @Test
    @DisplayName(" register — 400 con body vacío")
    void register_bodyVacio_retorna400() throws Exception {
        mockMvc.perform(post("/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }


    //  POST /usuarios/login


    @Test
    @DisplayName(" login — 200 con token JWT en la respuesta")
    void login_credencialesValidas_retornaToken() throws Exception {
        crearUsuario("login@test.com", "Pass123!", rolCliente);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("login@test.com");
        dto.setPassword("Pass123!");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@test.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andDo(print());
    }

    @Test
    @DisplayName(" login — el token JWT tiene formato válido (3 partes)")
    void login_tokenTieneFormatoJWT() throws Exception {
        crearUsuario("login@test.com", "Pass123!", rolCliente);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("login@test.com");
        dto.setPassword("Pass123!");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                // JWT tiene formato: xxxxx.yyyyy.zzzzz
                .andExpect(jsonPath("$.token").value(matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$")));
    }

    @Test
    @DisplayName(" login — 400 con email inexistente")
    void login_emailNoExiste_retorna400() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("noexiste@test.com");
        dto.setPassword("Pass123!");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName(" login — 400 con contraseña incorrecta")
    void login_passwordIncorrecta_retorna400() throws Exception {
        crearUsuario("login@test.com", "Pass123!", rolCliente);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("login@test.com");
        dto.setPassword("WrongPass!");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName(" login — 400 con body vacío")
    void login_bodyVacio_retorna400() throws Exception {
        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // Helper
    private UsuarioRegisterDTO buildRegisterDTO(String email) {
        UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
        dto.setEmail(email);
        dto.setPasswordHash("Pass123!");
        dto.setNombre("Juan Test");
        dto.setTelefono("123456789");
        dto.setDireccion("Calle 123");
        return dto;
    }
}