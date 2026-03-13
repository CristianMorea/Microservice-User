package com.ecomers.usuarios.TestUnitarios;



import com.ecomers.usuarios.Config.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName(" Permite los primeros 5 intentos")
    void permite_5_intentos() {
        String ip = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.intentoPermitido(ip),
                    "Intento " + (i + 1) + " debería ser permitido");
        }
    }

    @Test
    @DisplayName(" Bloquea el sexto intento")
    void bloquea_sexto_intento() {
        String ip = "192.168.1.2";
        for (int i = 0; i < 5; i++) {
            rateLimiter.intentoPermitido(ip);
        }
        assertFalse(rateLimiter.intentoPermitido(ip),
                "El sexto intento debería ser bloqueado");
    }

    @Test
    @DisplayName(" IPs distintas tienen buckets independientes")
    void ips_distintas_son_independientes() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // Agota ip1
        for (int i = 0; i < 5; i++) {
            rateLimiter.intentoPermitido(ip1);
        }

        // ip2 no debería verse afectada
        assertTrue(rateLimiter.intentoPermitido(ip2));
        assertFalse(rateLimiter.intentoPermitido(ip1));
    }
}
