package com.mossaiko.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class productcontrollertest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testControllerValidationSuccess() throws Exception {
        String requestBody = "{\"cadena\": \"J1011221226,Jabón Copito,200,990,96.954.210-2,gerencia@surlat.cl\"}";

        mockMvc.perform(post("/api/productos/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido", is(true)))
                .andExpect(jsonPath("$.mensaje", is("Producto validado correctamente")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    public void testControllerValidationFailure() throws Exception {
        // formato de codigo invalido, stock invalido, rut invalido, email invalido
        String requestBody = "{\"cadena\": \"J101122,Jabón Copito,-5,990,16.827.524-9,invalid-email\"}";

        mockMvc.perform(post("/api/productos/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido", is(false)))
                .andExpect(jsonPath("$.mensaje").doesNotExist())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.errores", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errores[0].campo", notNullValue()))
                .andExpect(jsonPath("$.errores[0].mensaje", notNullValue()));
    }
}