package com.marques.estoque.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.ProductRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User authenticateUser() {
        User user = new User("User Teste", "cat_user", "password123", UserRole.USER);
        User savedUser = userRepository.save(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(savedUser, null, savedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return savedUser;
    }

    @Test
    void getCategories_ShouldReturn401Unauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/category"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategories_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        authenticateUser();

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCategory_ShouldReturn200OK_WhenKeepingSameName() throws Exception {
        User user = authenticateUser();
        Category cat = new Category();
        cat.setName("Bebidas");
        cat.setUser(user);
        Category saved = categoryRepository.save(cat);

        CategoryDTO dto = new CategoryDTO();
        dto.setName("bebidas");

        mockMvc.perform(put("/category/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bebidas"));
    }

    @Test
    void updateCategory_ShouldReturn409Conflict_WhenDuplicateNameFromOtherCategory() throws Exception {
        User user = authenticateUser();

        Category cat1 = new Category();
        cat1.setName("Bebidas");
        cat1.setUser(user);
        categoryRepository.save(cat1);

        Category cat2 = new Category();
        cat2.setName("Lanches");
        cat2.setUser(user);
        Category saved2 = categoryRepository.save(cat2);

        CategoryDTO dto = new CategoryDTO();
        dto.setName("bebidas");

        mockMvc.perform(put("/category/{id}", saved2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_ShouldReturnError_WhenCategoryHasProducts() throws Exception {
        User user = authenticateUser();

        Category cat = new Category();
        cat.setName("Bebidas");
        cat.setUser(user);
        Category savedCat = categoryRepository.save(cat);

        Product prod = new Product();
        prod.setName("Água");
        prod.setQtd(10);
        prod.setCategories(savedCat);
        prod.setUser(user);
        productRepository.save(prod);

        mockMvc.perform(delete("/category/{id}", savedCat.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteCategory_ShouldReturn200OK_WhenCategoryIsEmpty() throws Exception {
        User user = authenticateUser();

        Category cat = new Category();
        cat.setName("Vazia");
        cat.setUser(user);
        Category savedCat = categoryRepository.save(cat);

        mockMvc.perform(delete("/category/{id}", savedCat.getId()))
                .andExpect(status().isOk());
    }
}
