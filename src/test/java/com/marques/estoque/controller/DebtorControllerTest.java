package com.marques.estoque.controller;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.PartialPaymentDTO;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.DebtorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DebtorControllerTest extends BaseControllerTest {

    @Autowired
    private DebtorRepository debtorRepository;

    @Test
    void getDebtors_ShouldReturn401Unauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/debtor"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDebtors_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        User user = createUser("User Teste", "debtor_user", "password123", UserRole.USER);

        mockMvc.perform(get("/debtor")
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk());
    }

    @Test
    void saveDebtor_ShouldReturn400BadRequest_WhenValueIsNegative() throws Exception {
        User user = createUser("User Teste", "debtor_neg", "password123", UserRole.USER);

        DebtorDTO dto = new DebtorDTO();
        dto.setName("Cliente Teste");
        dto.setValue(new BigDecimal("-10.00"));

        mockMvc.perform(post("/debtor")
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void partialPayment_ShouldReduceValue_AndSettleWhenZero() throws Exception {
        User user = createUser("User Teste", "debtor_pay", "password123", UserRole.USER);
        String token = bearerTokenFor(user);

        Debtor debtor = new Debtor();
        debtor.setName("Maria");
        debtor.setValue(new BigDecimal("100.00"));
        debtor.setDate(LocalDateTime.now());
        debtor.setUser(user);
        Debtor saved = debtorRepository.save(debtor);

        PartialPaymentDTO payment1 = new PartialPaymentDTO();
        payment1.setAmount(new BigDecimal("40.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", saved.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(60.0));

        PartialPaymentDTO payment2 = new PartialPaymentDTO();
        payment2.setAmount(new BigDecimal("60.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", saved.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment2)))
                .andExpect(status().isNoContent());
    }

    @Test
    void partialPayment_ShouldReturn400BadRequest_WhenAmountExceedsDebt() throws Exception {
        User user = createUser("User Teste", "debtor_excess", "password123", UserRole.USER);

        Debtor debtor = new Debtor();
        debtor.setName("Carlos");
        debtor.setValue(new BigDecimal("100.00"));
        debtor.setDate(LocalDateTime.now());
        debtor.setUser(user);
        Debtor saved = debtorRepository.save(debtor);

        PartialPaymentDTO excessPayment = new PartialPaymentDTO();
        excessPayment.setAmount(new BigDecimal("150.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", saved.getId())
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(excessPayment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void findDebtorsAndSummary_ShouldReturnFilteredDebtorsAndTotals() throws Exception {
        User user = createUser("User Teste", "debtor_search", "password123", UserRole.USER);
        String token = bearerTokenFor(user);

        Debtor d1 = new Debtor();
        d1.setName("Ana Paula");
        d1.setValue(new BigDecimal("80.00"));
        d1.setDate(LocalDateTime.now());
        d1.setUser(user);
        debtorRepository.save(d1);

        Debtor d2 = new Debtor();
        d2.setName("Bruno");
        d2.setValue(new BigDecimal("20.00"));
        d2.setDate(LocalDateTime.now());
        d2.setUser(user);
        debtorRepository.save(d2);

        mockMvc.perform(get("/debtor/pesquisar")
                .param("name", "ana")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtorDTOList.length()").value(1))
                .andExpect(jsonPath("$.debtorDTOList[0].name").value("Ana Paula"));

        mockMvc.perform(get("/debtor/summary")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDebtors").value(2))
                .andExpect(jsonPath("$.totalValue").value(100.0));
    }

    @Test
    void settleAndTransactions_ShouldKeepHistory_MoveToPaid_AndReopenSameAccount() throws Exception {
        User user = createUser("User Extrato", "debtor_history_user", "password123", UserRole.USER);
        String token = bearerTokenFor(user);

        DebtorDTO createDto = new DebtorDTO();
        createDto.setName("Fernanda Lima");
        createDto.setDescription("1 Arroz 5kg, 2 Feijão");
        createDto.setValue(new BigDecimal("120.00"));

        String createdJson = mockMvc.perform(post("/debtor")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paid").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long debtorId = objectMapper.readTree(createdJson).get("id").asLong();

        PartialPaymentDTO partial = new PartialPaymentDTO();
        partial.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(patch("/debtor/{id}/pay", debtorId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(70.0));

        mockMvc.perform(patch("/debtor/{id}/settle", debtorId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.value").value(0.0));

        mockMvc.perform(get("/debtor/pesquisar")
                .param("paid", "false")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtorDTOList.length()").value(0));

        mockMvc.perform(get("/debtor/pesquisar")
                .param("paid", "true")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtorDTOList.length()").value(1))
                .andExpect(jsonPath("$.debtorDTOList[0].name").value("Fernanda Lima"));

        mockMvc.perform(get("/debtor/{id}/transactions", debtorId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].transactionType").value("QUITACAO"))
                .andExpect(jsonPath("$[1].transactionType").value("ABATIMENTO"))
                .andExpect(jsonPath("$[2].transactionType").value("LANCAMENTO"));

        DebtorDTO reopenDto = new DebtorDTO();
        reopenDto.setName("fernanda lima");
        reopenDto.setDescription("1 Café 500g");
        reopenDto.setValue(new BigDecimal("25.00"));

        mockMvc.perform(post("/debtor")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reopenDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(debtorId))
                .andExpect(jsonPath("$.paid").value(false))
                .andExpect(jsonPath("$.value").value(25.0));

        mockMvc.perform(get("/debtor/{id}/transactions", debtorId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].transactionType").value("LANCAMENTO"));
    }
}
