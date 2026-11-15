package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.dto.SummaryProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductDTO findById(Long id) {
        log.info("Buscando produto por id para o usuário logado");
        return productMapper.toDTO(returnProductWithId(id, authenticatedUserProvider.getCurrentUser()));
    }

    public List<ProductDTO> findByName(String name) {
        log.info("Buscando produto por nome para o usuário logado");
        return productMapper.toDTOList(
                productRepository.findByNameContainingIgnoreCaseAndUser(name, authenticatedUserProvider.getCurrentUser())
        );
    }

    public ProductDTO save(ProductDTO productDTO) {
        log.info("Salvando produto no banco de dados para o usuário logado");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        validateProduct(productDTO.getName(), productDTO.getQtd());

        if (productRepository.existsByNameIgnoreCaseAndUser(productDTO.getName(), currentUser)) {
            throw new DuplicateDataException("Já existe um produto cadastrado com esse nome.");
        }

        Product product = productMapper.toEntity(productDTO);
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), currentUser));
        product.setUser(currentUser);

        return productMapper.toDTO(productRepository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {} no banco de dados", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Product product = returnProductWithId(id, currentUser);

        validateProduct(productDTO.getName(), productDTO.getQtd());

        if (productRepository.existsByNameIgnoreCaseAndUserAndIdNot(productDTO.getName(), currentUser, id)) {
            throw new DuplicateDataException("Já existe um produto cadastrado com esse nome.");
        }

        updateProductDTO(product, productDTO, currentUser);

        return productMapper.toDTO(productRepository.save(product));
    }

    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        if (!productRepository.existsByIdAndUser(id, authenticatedUserProvider.getCurrentUser())) {
            log.error("Produto não encontrado com o id: {}", id);
            throw new NotFoundException("Produto com o id " + id + " não encontrado");
        }

        productRepository.deleteById(id);
        return "Produto com id " + id + " deletado com sucesso";
    }

    public SummaryProductDTO summaryProduct() {
        log.info("Contabilizando dados dos produtos");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Integer total = productRepository.countTotalProductsByUser(currentUser);
        Integer lowStock = productRepository.countLowStockProductsByUser(currentUser, LOW_STOCK_THRESHOLD);
        Integer noStock = productRepository.countOutOfStockProductsByUser(currentUser);

        return new SummaryProductDTO(total, lowStock, noStock);
    }

    public List<ProductDTO> findAll() {
        List<Product> productList = productRepository.findAllByUserOrderByIdAsc(authenticatedUserProvider.getCurrentUser());
        return productMapper.toDTOList(productList);
    }

    public List<ProductDTO> lowStockProductFilter() {
        List<Product> lowStockProducts = productRepository.findLowStockProductsOrderByIdAsc(
                authenticatedUserProvider.getCurrentUser(), LOW_STOCK_THRESHOLD
        );
        return productMapper.toDTOList(lowStockProducts);
    }

    public List<ProductDTO> noStockProductFilter() {
        List<Product> noStockProducts = productRepository.findOutOfStockProductsOrderByIdAsc(
                authenticatedUserProvider.getCurrentUser()
        );
        return productMapper.toDTOList(noStockProducts);
    }

    private Product returnProductWithId(Long id, User user) {
        return productRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Produto com o id: " + id + " não encontrado"));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO, User currentUser) {
        product.setName(productDTO.getName());
        product.setQtd(productDTO.getQtd());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), currentUser));
    }

    private void validateProduct(String name, Integer qtd) {
        if (name == null || name.trim().isEmpty()) {
            log.error("O nome do produto não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (qtd == null || qtd < 0) {
            log.error("A quantidade do produto não pode ser nula ou negativa");
            throw new ArgumentException("A quantidade do produto deve ser maior ou igual a zero");
        }
    }
}
