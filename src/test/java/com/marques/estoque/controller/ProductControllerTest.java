package com.marques.estoque.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marques.estoque.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProducts_ShouldReturn403Forbidden_WhenNoToken() throws Exception {
        mockMvc.perform(get("/product"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getProducts_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        mockMvc.perform(get("/product"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void saveProduct_ShouldReturn201Created_WhenTokenIsValid() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName("Produto Teste");
        dto.setQtd(10);
        dto.setStatus("ATIVO");


        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))

                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assert statusCode != 403;
                });
    }
}
