package com.marques.estoque.controller;

import com.marques.estoque.dto.LoginRequestDTO;
import com.marques.estoque.dto.ProfileUpdateDTO;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends BaseControllerTest {

    @Test
    void register_ShouldReturn200OK() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void register_ShouldReturn409Conflict_WhenUsernameExists() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Maria");
        dto.setUsername("maria_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_ShouldReturn400BadRequest_WhenPasswordIsBlank() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_sem_senha");
        dto.setPassword("   ");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_ShouldReturn200AndTokenAndName_WhenCredentialsAreValid() throws Exception {
        createUser("Sávio Marques", "savio_login", "SenhaForte123", UserRole.USER);

        LoginRequestDTO loginDto = new LoginRequestDTO();
        loginDto.setUsername("savio_login");
        loginDto.setPassword("SenhaForte123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.name").value("Sávio Marques"))
                .andExpect(jsonPath("$.username").value("savio_login"));
    }

    @Test
    void login_ShouldReturn401Unauthorized_WhenCredentialsWrong() throws Exception {
        LoginRequestDTO loginDto = new LoginRequestDTO();
        loginDto.setUsername("naoexiste");
        loginDto.setPassword("errada");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos."));
    }

    @Test
    void getProfile_ShouldReturnUserData_WhenAuthenticatedWithBearerToken() throws Exception {
        User user = createUser("Maria Perfil", "maria_me", "senha123", UserRole.USER);

        mockMvc.perform(get("/auth/me")
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Perfil"))
                .andExpect(jsonPath("$.username").value("maria_me"));
    }

    @Test
    void getProfile_ShouldReturn401Unauthorized_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_ShouldUpdateNameAndPassword_WhenCurrentPasswordMatches() throws Exception {
        User user = createUser("Nome Antigo", "perfil_update", "senhaAntiga123", UserRole.USER);
        String token = bearerTokenFor(user);

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setName("Nome Novo");
        dto.setCurrentPassword("senhaAntiga123");
        dto.setNewPassword("senhaNova456");

        mockMvc.perform(put("/auth/me")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Novo"));

        LoginRequestDTO loginWithNewPassword = new LoginRequestDTO();
        loginWithNewPassword.setUsername("perfil_update");
        loginWithNewPassword.setPassword("senhaNova456");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginWithNewPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void updateProfile_ShouldReturn400BadRequest_WhenCurrentPasswordIsWrong() throws Exception {
        User user = createUser("Usuário Teste", "perfil_erro", "senhaCorreta", UserRole.USER);

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setName("Outro Nome");
        dto.setCurrentPassword("senhaErrada");
        dto.setNewPassword("novaSenha");

        mockMvc.perform(put("/auth/me")
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha atual está incorreta."));
    }
}
