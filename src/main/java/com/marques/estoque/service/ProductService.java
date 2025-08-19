package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.dto.SummaryProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.util.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductMapper productMapper;

    private static final int LOW_STOCK_THRESHOLD = 5;

    public ProductDTO findById(Long id) {
        log.info("Buscando produto por id para o usuário logado");
        return ProductMapper.INSTANCE.toDTO(returnProductWithId(id, getCurrentUser()));
    }

    public ProductDTO findByName(String name) {
        log.info("Buscando produto por nome para o usuário logado");
        return ProductMapper.INSTANCE.toDTO(returnProductWithName(name, getCurrentUser()));
    }

    public List<ProductDTO> findAll() {
        log.info("Buscando todos os produtos para o usuário logado");
        List<Product> listProduct = productRepository.findAllByUser(getCurrentUser());

        if(listProduct.isEmpty()) {
            log.warn("Nenhum produto encontrado");
            throw new NotFoundException("Nenhum produto encontrado");
        }

        return ProductMapper.INSTANCE.toDTOList(listProduct);
    }

    public ProductDTO save(ProductDTO productDTO) {
        log.info("Salvando produto no banco de dados para o usuário logado");

        validateProduct(productDTO.getName(), productDTO.getQtd() ,productDTO.getCategoryId());

        if (productRepository.existsByNameAndUser(productDTO.getName(), getCurrentUser())){
            Product product = returnProductWithName(productDTO.getName(), getCurrentUser());

            product.setQtd(productDTO.getQtd() + product.getQtd());

            return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
        }

        Product product = productMapper.toEntity(productDTO);

        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), getCurrentUser()));
        product.setUser(getCurrentUser());

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {} no banco de dados", id);
        Product product = returnProductWithId(id, getCurrentUser());

        validateProduct(productDTO.getName(), productDTO.getQtd() ,productDTO.getCategoryId());

        updateProductDTO(product, productDTO);

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO updatePatch(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {} no banco de dados", id);

        Product product = returnProductWithId(id, getCurrentUser());

        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getQtd() != null) {
            product.setQtd(productDTO.getQtd());
        }
        if (productDTO.getCategoryId() != null) {
            product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), getCurrentUser()));
        }

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        if (!productRepository.existsByIdAndUser(id, getCurrentUser())){
            log.error("Produto não encontrado com o id: {}",id);
            throw new NotFoundException("Produto com o id " + id + " não encontrado");
        }

        productRepository.deleteById(id);
        return "Produto com id " + id + " deletado com sucesso";
    }

    public SummaryProductDTO summaryProduct(){

        Integer total = productRepository.countTotalProducts(getCurrentUser());
        Integer lowStock = productRepository.countLowStockProducts(getCurrentUser(), LOW_STOCK_THRESHOLD);
        Integer noStock = productRepository.countOutOfStockProducts(getCurrentUser());

        return new SummaryProductDTO(total, lowStock, noStock);
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Product returnProductWithId(Long id, User user) {
        return productRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Produto com o id: " + id + " não encontrado"));
    }

    private Product returnProductWithName(String name, User user) {
        return productRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Produto com o nome: "+ name + " não encontrado"));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setQtd(productDTO.getQtd());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), getCurrentUser()));
    }

    private void validateProduct(String name, Integer qtd ,Long id) {
        if (name == null || name.isEmpty()) {
            log.error("O nome do produto não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (qtd == null) {
            log.error("A quantidade do produto não pode ser nula");
            throw new ArgumentException("A quantidade do produto não pode ser nulo");
        }

        if (id == null) {
            log.error("O Id da categoria não pode ser nulo");
            throw new ArgumentException("O Id da categoria não pode ser nulo");
        }
    }

}
