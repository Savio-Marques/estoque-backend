package com.marques.estoque.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DebtorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDebtors_ShouldReturn403Forbidden_WhenNoToken() throws Exception {
        mockMvc.perform(get("/debtor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getDebtors_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        mockMvc.perform(get("/debtor"))
                .andExpect(status().isOk());
    }
}
