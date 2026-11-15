package com.marques.estoque.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.PartialPaymentDTO;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DebtorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DebtorRepository debtorRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User authenticateUser() {
        User user = new User("User Teste", "debtor_user", "password123", UserRole.USER);
        User savedUser = userRepository.save(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(savedUser, null, savedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return savedUser;
    }

    @Test
    void getDebtors_ShouldReturn401Unauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/debtor"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDebtors_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        authenticateUser();

        mockMvc.perform(get("/debtor"))
                .andExpect(status().isOk());
    }

    @Test
    void saveDebtor_ShouldReturn400BadRequest_WhenValueIsNegative() throws Exception {
        authenticateUser();

        DebtorDTO dto = new DebtorDTO();
        dto.setName("Cliente Teste");
        dto.setValue(new BigDecimal("-10.00"));

        mockMvc.perform(post("/debtor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void partialPayment_ShouldReduceValue_AndSettleWhenZero() throws Exception {
        User user = authenticateUser();

        Debtor debtor = new Debtor();
        debtor.setName("Maria");
        debtor.setValue(new BigDecimal("100.00"));
        debtor.setDate(LocalDateTime.now());
        debtor.setUser(user);
        Debtor saved = debtorRepository.save(debtor);

        PartialPaymentDTO payment1 = new PartialPaymentDTO();
        payment1.setAmount(new BigDecimal("40.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(60.0));

        PartialPaymentDTO payment2 = new PartialPaymentDTO();
        payment2.setAmount(new BigDecimal("60.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment2)))
                .andExpect(status().isNoContent());
    }
}
