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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends BaseControllerTest {

    @Test
    void register_ShouldReturn401Unauthorized_WhenUnauthenticated() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_publico");
        dto.setPassword("senha123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_ShouldReturn403Forbidden_WhenRegularUserTriesToRegister() throws Exception {
        User regularUser = createUser("Comum", "comum_reg", "senha123", UserRole.USER);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_bloqueado");
        dto.setPassword("senha123");

        mockMvc.perform(post("/auth/register")
                .header("Authorization", bearerTokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_ShouldReturn200OK_WhenAdminRegistersUser() throws Exception {
        User admin = createUser("Root", "admin_reg", "senha123", UserRole.ADMIN);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                .header("Authorization", bearerTokenFor(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao_auth"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_ShouldReturn409Conflict_WhenUsernameExists() throws Exception {
        User admin = createUser("Root", "admin_dup_reg", "senha123", UserRole.ADMIN);
        String adminToken = bearerTokenFor(admin);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Maria");
        dto.setUsername("maria_auth");
        dto.setPassword("senha123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_ShouldReturn400BadRequest_WhenPasswordIsBlank() throws Exception {
        User admin = createUser("Root", "admin_blank_pwd", "senha123", UserRole.ADMIN);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("João");
        dto.setUsername("joao_sem_senha");
        dto.setPassword("   ");

        mockMvc.perform(post("/auth/register")
                .header("Authorization", bearerTokenFor(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_ShouldReturn200AndTokenAndNameAndRole_WhenCredentialsAreValid() throws Exception {
        createUser("Sávio Marques", "savio_login", "SenhaForte123", UserRole.ADMIN);

        LoginRequestDTO loginDto = new LoginRequestDTO();
        loginDto.setUsername("savio_login");
        loginDto.setPassword("SenhaForte123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.name").value("Sávio Marques"))
                .andExpect(jsonPath("$.username").value("savio_login"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
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

    @Test
    void login_ShouldReturn429TooManyRequests_AfterFiveFailedAttempts() throws Exception {
        createUser("Alvo Brute Force", "usuario_bruteforce", "SenhaSecreta123", UserRole.USER);

        LoginRequestDTO wrongLogin = new LoginRequestDTO();
        wrongLogin.setUsername("usuario_bruteforce");
        wrongLogin.setPassword("errada123");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        }

        LoginRequestDTO evenRightPassword = new LoginRequestDTO();
        evenRightPassword.setUsername("usuario_bruteforce");
        evenRightPassword.setPassword("SenhaSecreta123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evenRightPassword)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").exists());
    }
}
