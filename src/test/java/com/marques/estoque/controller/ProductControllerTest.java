package com.marques.estoque.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marques.estoque.dto.ProductDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

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
        User user = new User("User Teste", "product_user", "password123", UserRole.USER);
        User savedUser = userRepository.save(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(savedUser, null, savedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return savedUser;
    }

    private Category createCategory(User user, String name) {
        Category cat = new Category();
        cat.setName(name);
        cat.setUser(user);
        return categoryRepository.save(cat);
    }

    @Test
    void getProducts_ShouldReturn401Unauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(get("/product"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProducts_ShouldReturn200OK_WhenTokenIsValid() throws Exception {
        authenticateUser();

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk());
    }

    @Test
    void saveProduct_ShouldReturn201Created_WhenAuthenticatedAndValid() throws Exception {
        User user = authenticateUser();
        Category cat = createCategory(user, "Bebidas");

        ProductDTO dto = new ProductDTO();
        dto.setName("Produto Teste");
        dto.setQtd(10);
        dto.setCategoryId(cat.getId());

        mockMvc.perform(post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Produto Teste"));
    }

    @Test
    void saveProduct_ShouldReturn400BadRequest_WhenInvalidDTO() throws Exception {
        authenticateUser();

        ProductDTO dto = new ProductDTO();
        dto.setName("Produto Sem Categoria");
        dto.setQtd(-5);

        mockMvc.perform(post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateProduct_ShouldReturn200OK_WhenKeepingSameName() throws Exception {
        User user = authenticateUser();
        Category cat = createCategory(user, "Bebidas");

        Product p = new Product();
        p.setName("Coca Cola");
        p.setQtd(10);
        p.setCategories(cat);
        p.setUser(user);
        Product saved = productRepository.save(p);

        ProductDTO dto = new ProductDTO();
        dto.setName("coca cola");
        dto.setQtd(15);
        dto.setCategoryId(cat.getId());

        mockMvc.perform(put("/product/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtd").value(15));
    }

    @Test
    void updateProduct_ShouldReturn409Conflict_WhenDuplicateNameFromOtherProduct() throws Exception {
        User user = authenticateUser();
        Category cat = createCategory(user, "Bebidas");

        Product p1 = new Product();
        p1.setName("Coca Cola");
        p1.setQtd(10);
        p1.setCategories(cat);
        p1.setUser(user);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Guaraná");
        p2.setQtd(5);
        p2.setCategories(cat);
        p2.setUser(user);
        Product saved2 = productRepository.save(p2);

        ProductDTO dto = new ProductDTO();
        dto.setName("coca cola");
        dto.setQtd(5);
        dto.setCategoryId(cat.getId());

        mockMvc.perform(put("/product/{id}", saved2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}
