package com.marques.estoque.controller;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends BaseControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

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
        User user = createUser("User Teste", "product_user", "password123", UserRole.USER);

        mockMvc.perform(get("/product")
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk());
    }

    @Test
    void saveProduct_ShouldReturn201Created_WhenAuthenticatedAndValid() throws Exception {
        User user = createUser("User Teste", "product_save", "password123", UserRole.USER);
        Category cat = createCategory(user, "Bebidas");

        ProductDTO dto = new ProductDTO();
        dto.setName("Produto Teste");
        dto.setQtd(10);
        dto.setCategoryId(cat.getId());

        mockMvc.perform(post("/product")
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Produto Teste"));
    }

    @Test
    void saveProduct_ShouldReturn400BadRequest_WhenInvalidDTO() throws Exception {
        User user = createUser("User Teste", "product_invalid", "password123", UserRole.USER);

        ProductDTO dto = new ProductDTO();
        dto.setName("Produto Sem Categoria");
        dto.setQtd(-5);

        mockMvc.perform(post("/product")
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateProduct_ShouldReturn200OK_WhenKeepingSameName() throws Exception {
        User user = createUser("User Teste", "product_upd", "password123", UserRole.USER);
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
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtd").value(15));
    }

    @Test
    void updateProduct_ShouldReturn409Conflict_WhenDuplicateNameFromOtherProduct() throws Exception {
        User user = createUser("User Teste", "product_dup", "password123", UserRole.USER);
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
                .header("Authorization", bearerTokenFor(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void summaryProducts_ShouldReturnExactCounts() throws Exception {
        User user = createUser("User Teste", "product_sum", "password123", UserRole.USER);
        Category cat = createCategory(user, "Bebidas");

        Product available = new Product();
        available.setName("Água 500ml");
        available.setQtd(20);
        available.setCategories(cat);
        available.setUser(user);
        productRepository.save(available);

        Product lowStock = new Product();
        lowStock.setName("Suco");
        lowStock.setQtd(3);
        lowStock.setCategories(cat);
        lowStock.setUser(user);
        productRepository.save(lowStock);

        Product noStock = new Product();
        noStock.setName("Refrigerante");
        noStock.setQtd(0);
        noStock.setCategories(cat);
        noStock.setUser(user);
        productRepository.save(noStock);

        mockMvc.perform(get("/product/summary")
                .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduct").value(3))
                .andExpect(jsonPath("$.lowStock").value(1))
                .andExpect(jsonPath("$.noStock").value(1));
    }

    @Test
    void saveProductWithPricesAndMinQuantity_ShouldCalculateSummaryAndRecordFullMovementLifecycle() throws Exception {
        User user = createUser("User Teste", "product_mov", "password123", UserRole.USER);
        String token = bearerTokenFor(user);
        Category cat = createCategory(user, "Mercearia");

        ProductDTO dto = new ProductDTO();
        dto.setName("Arroz 5kg");
        dto.setQtd(8);
        dto.setCostPrice(new BigDecimal("15.00"));
        dto.setSalePrice(new BigDecimal("25.00"));
        dto.setMinQuantity(10);
        dto.setCategoryId(cat.getId());

        MvcResult createdResult = mockMvc.perform(post("/product")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Estoque Baixo"))
                .andReturn();

        Long productId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/product/summary")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").value(1))
                .andExpect(jsonPath("$.totalStockValue").value(120.0))
                .andExpect(jsonPath("$.totalSaleValue").value(200.0));

        dto.setQtd(12);
        mockMvc.perform(put("/product/{id}", productId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Disponível"));

        dto.setQtd(6);
        mockMvc.perform(put("/product/{id}", productId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/product/{id}", productId)
                .header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/product/movements")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[?(@.movementType == 'CADASTRO')].quantityDelta").value(8))
                .andExpect(jsonPath("$[?(@.movementType == 'ENTRADA')].quantityDelta").value(4))
                .andExpect(jsonPath("$[?(@.movementType == 'SAIDA')].quantityDelta").value(-6))
                .andExpect(jsonPath("$[?(@.movementType == 'EXCLUSAO')].quantityDelta").value(-6));
    }

    @Test
    void multiTenantIsolation_ShouldReturn404_WhenOtherUserTriesToAccessProduct() throws Exception {
        User owner = createUser("Dono", "prod_owner", "password123", UserRole.USER);
        User intruder = createUser("Invasor", "prod_intruder", "password123", UserRole.USER);
        Category cat = createCategory(owner, "Bebidas");

        Product p = new Product();
        p.setName("Produto Privado");
        p.setQtd(10);
        p.setCategories(cat);
        p.setUser(owner);
        Product saved = productRepository.save(p);

        mockMvc.perform(get("/product/{id}", saved.getId())
                .header("Authorization", bearerTokenFor(intruder)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/product/{id}", saved.getId())
                .header("Authorization", bearerTokenFor(intruder)))
                .andExpect(status().isNotFound());
    }
}
