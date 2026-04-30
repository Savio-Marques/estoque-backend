package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.dto.SummaryProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.repository.ProductSummaryProjection;
import com.marques.estoque.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    private static final int LOW_STOCK_THRESHOLD = 5;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        log.info("Buscando produto por id");
        return productMapper.toDTO(returnProduct(id, getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        log.info("Buscando todos os produtos");
        List<Product> productList = productRepository.findAllByUserOrderByIdAsc(getCurrentUser());
        return productMapper.toDTOList(productList);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findByName(String name) {
        log.info("Buscando produto por nome");
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndUser(normalizeName(name), getCurrentUser());
        return productMapper.toDTOList(products);
    }

    @Transactional
    public ProductDTO save(ProductDTO productDTO) {
        log.info("Salvando produto");

        String name = normalizeName(productDTO.getName());
        validateForCreate(name, productDTO.getQtd());

        Product product = productMapper.toEntity(productDTO);
        product.setName(name);
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), getCurrentUser()));
        product.setUser(getCurrentUser());

        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {}", id);
        Product product = returnProduct(id, getCurrentUser());

        String name = normalizeName(productDTO.getName());
        validateForUpdate(id, name, productDTO.getQtd());

        product.setName(name);
        product.setQtd(productDTO.getQtd());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), getCurrentUser()));

        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando id: {}", id);
        returnProduct(id, getCurrentUser());
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SummaryProductDTO summaryProduct() {
        log.info("Processando sumário de produtos");

        ProductSummaryProjection projection = productRepository.getProductSummary(getCurrentUser(), LOW_STOCK_THRESHOLD);

        return new SummaryProductDTO(
                projection.getTotal(),
                projection.getLowStock(),
                projection.getNoStock()
        );
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Product returnProduct(Long id, User user) {
        return productRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Produto com id " + id + " não encontrado"));
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim();
    }

    private void validateNameNotBlank(String name) {
        if (name == null || name.isBlank()) {
            log.error("Nome do produto nulo ou vazio");
            throw new ArgumentException("Nome do produto não pode ser vazio");
        }
    }

    private void validateQuantity(Integer qtd) {
        if (qtd == null || qtd < 0) {
            log.error("Quantidade inválida");
            throw new ArgumentException("A quantidade do produto deve ser informada e maior ou igual a zero");
        }
    }

    private void validateForCreate(String name, Integer qtd) {
        validateNameNotBlank(name);
        validateQuantity(qtd);

        if (productRepository.findByNameIgnoreCaseAndUser(name, getCurrentUser()).isPresent()) {
            log.error("Produto já existente");
            throw new DuplicateDataException("Produto '" + name + "' já cadastrado");
        }
    }

    private void validateForUpdate(Long id, String name, Integer qtd) {
        validateNameNotBlank(name);
        validateQuantity(qtd);

        var existing = productRepository.findByNameIgnoreCaseAndUser(name, getCurrentUser());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            log.error("Conflito de nome na atualização");
            throw new DuplicateDataException("Produto '" + name + "' já cadastrado em outro ID");
        }
    }
}
