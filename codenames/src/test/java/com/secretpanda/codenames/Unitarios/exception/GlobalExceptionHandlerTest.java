package com.secretpanda.codenames.Unitarios.exception;

import com.secretpanda.codenames.exception.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Test de integración ligero para validar que el manejador global de excepciones
 * formatea correctamente las respuestas HTTP según el tipo de error.
 */
@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@ContextConfiguration(classes = {GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn404AndJsonFormatWhenNotFoundExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado"));
    }

    @Test
    void shouldReturn400AndJsonFormatWhenBadRequestExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Petición inválida"));
    }

    @Test
    void shouldReturn409AndJsonFormatWhenGameLogicExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/game-logic"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Regla de juego violada"));
    }

    @Test
    void shouldReturn400AndDetailedErrorsWhenBeanValidationFails() throws Exception {
        String invalidPayload = "{\"nombre\": \"\", \"edad\": 10}"; // nombre en blanco (NotBlank)

        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details.nombre").value("no debe estar en blanco"));
    }

    // --- Configuraciones Mock para forzar las excepciones ---

    @Configuration
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new NotFoundException("Recurso no encontrado");
        }

        @GetMapping("/test/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Petición inválida");
        }

        @GetMapping("/test/game-logic")
        public void throwGameLogic() {
            throw new GameLogicException("Regla de juego violada");
        }

        @PostMapping("/test/validation")
        public void throwValidation(@Valid @RequestBody TestDTO dto) {
            // No hace nada, fallará en la validación @Valid antes de entrar si es incorrecto
        }
    }

    static class TestDTO {
        @NotBlank(message = "no debe estar en blanco")
        private String nombre;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }
}