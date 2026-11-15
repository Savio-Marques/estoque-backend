package com.marques.estoque.controller;

import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.product.StockMovement;
import com.marques.estoque.model.product.StockMovementType;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.repository.StockMovementRepository;
import com.marques.estoque.security.AdminBootstrapRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DebtorRepository debtorRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private AdminBootstrapRunner adminBootstrapRunner;

    @Test
    void getUsers_ShouldReturn403Forbidden_WhenUserIsNotAdmin() throws Exception {
        User regularUser = createUser("Comum", "user_comum", "senha123", UserRole.USER);

        mockMvc.perform(get("/users")
                .header("Authorization", bearerTokenFor(regularUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_ShouldReturn200OK_WhenUserIsAdmin() throws Exception {
        User admin = createUser("Admin", "admin_users", "senha123", UserRole.ADMIN);

        mockMvc.perform(get("/users")
                .header("Authorization", bearerTokenFor(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturn403Forbidden_WhenUserIsNotAdmin() throws Exception {
        User regularUser = createUser("Comum", "user_sem_perm", "senha123", UserRole.USER);
        User target = createUser("Alvo", "user_alvo", "senha123", UserRole.USER);

        mockMvc.perform(delete("/users/{id}", target.getId())
                .header("Authorization", bearerTokenFor(regularUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ShouldCascadeAllDependentRecords_WhenUserIsAdmin() throws Exception {
        User admin = createUser("Admin", "admin_del", "senha123", UserRole.ADMIN);
        User target = createUser("Comerciante", "comerciante_del", "senha123", UserRole.USER);

        Category cat = new Category();
        cat.setName("Bebidas");
        cat.setUser(target);
        Category savedCat = categoryRepository.save(cat);

        Product prod = new Product();
        prod.setName("Refrigerante");
        prod.setQtd(12);
        prod.setCategories(savedCat);
        prod.setUser(target);
        productRepository.save(prod);

        StockMovement movement = new StockMovement();
        movement.setProductName("Refrigerante");
        movement.setMovementType(StockMovementType.CADASTRO);
        movement.setPreviousQuantity(0);
        movement.setNewQuantity(12);
        movement.setQuantityDelta(12);
        movement.setCreatedAt(LocalDateTime.now());
        movement.setUser(target);
        stockMovementRepository.save(movement);

        Debtor debtor = new Debtor();
        debtor.setName("Cliente Fiado");
        debtor.setValue(new BigDecimal("50.00"));
        debtor.setDate(LocalDateTime.now());
        debtor.setUser(target);
        debtorRepository.save(debtor);

        mockMvc.perform(delete("/users/{id}", target.getId())
                .header("Authorization", bearerTokenFor(admin)))
                .andExpect(status().isOk());

        assertFalse(userRepository.existsById(target.getId()));
    }

    @Test
    void updateUser_ShouldReturn409Conflict_WhenUsernameAlreadyBelongsToAnotherUser() throws Exception {
        User admin = createUser("Admin", "admin_upd", "senha123", UserRole.ADMIN);
        createUser("Primeiro", "login_existente", "senha123", UserRole.USER);
        User second = createUser("Segundo", "login_segundo", "senha123", UserRole.USER);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Segundo Atualizado");
        dto.setUsername("login_existente");
        dto.setPassword("nova123");
        dto.setRole(UserRole.USER);

        mockMvc.perform(put("/users/{id}", second.getId())
                .header("Authorization", bearerTokenFor(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_ShouldReturn400BadRequest_WhenAdminTriesToDeleteSelf() throws Exception {
        User admin = createUser("Root", "admin_self_del", "senha123", UserRole.ADMIN);

        mockMvc.perform(delete("/users/{id}", admin.getId())
                .header("Authorization", bearerTokenFor(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Você não pode excluir sua própria conta."));
    }

    @Test
    void updateUser_ShouldReturn400BadRequest_WhenAdminTriesToDemoteSelf() throws Exception {
        User admin = createUser("Root", "admin_self_demote", "senha123", UserRole.ADMIN);

        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Root");
        dto.setUsername("admin_self_demote");
        dto.setRole(UserRole.USER);

        mockMvc.perform(put("/users/{id}", admin.getId())
                .header("Authorization", bearerTokenFor(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Você não pode remover seu próprio privilégio de administrador."));
    }

    @Test
    void adminBootstrapRunner_ShouldCreateNewAdminAndPromoteExistingUser() {
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", "root_bootstrap");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminPassword", "SenhaRoot123");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminName", "Sávio Root");

        adminBootstrapRunner.run(new DefaultApplicationArguments());

        User created = (User) userRepository.findByUsername("root_bootstrap");
        assertNotNull(created);
        assertEquals(UserRole.ADMIN, created.getRole());
        assertEquals("Sávio Root", created.getName());

        User existing = createUser("Existente", "ja_existe_user", "senha123", UserRole.USER);
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", existing.getUsername());
        adminBootstrapRunner.run(new DefaultApplicationArguments());

        User promoted = (User) userRepository.findByUsername("ja_existe_user");
        assertEquals(UserRole.ADMIN, promoted.getRole());

        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", "");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminPassword", "");
    }
}
