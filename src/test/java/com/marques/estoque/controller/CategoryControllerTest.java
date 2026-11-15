package com.marques.estoque.controller;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends BaseControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void getCategories_ShouldReturn401Unauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/category"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategories_ShouldReturn401Unauthorized_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/category")
                .header("Authorization", "Bearer token-invalido-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategories_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        User user = createUser("User Teste", "cat_user", "password123", UserRole.USER);

        mockMvc.perform(get("/category")
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCategory_ShouldReturn200OK_WhenKeepingSameName() throws Exception {
        User user = createUser("User Teste", "cat_same", "password123", UserRole.USER);
        Category cat = new Category();
        cat.setName("Bebidas");
        cat.setUser(user);
        Category saved = categoryRepository.save(cat);

        CategoryDTO dto = new CategoryDTO();
        dto.setName("bebidas");

        mockMvc.perform(put("/category/{id}", saved.getId())
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bebidas"));
    }

    @Test
    void updateCategory_ShouldReturn409Conflict_WhenDuplicateNameFromOtherCategory() throws Exception {
        User user = createUser("User Teste", "cat_dup", "password123", UserRole.USER);

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
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_ShouldReturnError_WhenCategoryHasProducts() throws Exception {
        User user = createUser("User Teste", "cat_prod", "password123", UserRole.USER);

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

        mockMvc.perform(delete("/category/{id}", savedCat.getId())
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteCategory_ShouldReturn200OK_WhenCategoryIsEmpty() throws Exception {
        User user = createUser("User Teste", "cat_empty", "password123", UserRole.USER);

        Category cat = new Category();
        cat.setName("Vazia");
        cat.setUser(user);
        Category savedCat = categoryRepository.save(cat);

        mockMvc.perform(delete("/category/{id}", savedCat.getId())
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk());
    }

    @Test
    void multiTenantIsolation_ShouldReturn404_WhenOtherUserTriesToEditOrDeleteCategory() throws Exception {
        User owner = createUser("Dono", "cat_owner", "password123", UserRole.USER);
        User intruder = createUser("Outro", "cat_intruder", "password123", UserRole.USER);

        Category cat = new Category();
        cat.setName("Privada");
        cat.setUser(owner);
        Category savedCat = categoryRepository.save(cat);

        CategoryDTO dto = new CategoryDTO();
        dto.setName("Hackeada");

        mockMvc.perform(put("/category/{id}", savedCat.getId())
                .header("Authorization", bearerTokenFor(intruder))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/category/{id}", savedCat.getId())
                .header("Authorization", bearerTokenFor(intruder)))
                .andExpect(status().isNotFound());
    }
}
