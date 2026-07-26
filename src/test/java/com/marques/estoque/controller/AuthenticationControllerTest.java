package com.marques.estoque.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldReturn200OK() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void register_ShouldReturn409Conflict_WhenUsernameExists() throws Exception {
        // Cadastra o primeiro
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Maria");
        dto.setUsername("maria_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Tenta cadastrar de novo com mesmo username
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // 409
    }

    @Test
    void login_ShouldReturn403Forbidden_WhenCredentialsWrong() throws Exception {
        UserCreateDTO loginDto = new UserCreateDTO();
        loginDto.setUsername("naoexiste");
        loginDto.setPassword("errada");

        String json = objectMapper.writeValueAsString(loginDto);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }
}
