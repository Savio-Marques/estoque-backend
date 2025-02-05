package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Product;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.util.ProductMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    private final ProductMapper productMapper;


    public ProductDTO findById(Long id) {
        log.info("Searching product by id");
        return ProductMapper.INSTANCE.toDTO(returnProduct(id));
    }

    public List<ProductDTO> findAll() {
        log.info("Searching all products");
        List<Product> listProduct = productRepository.findAll();

        if(listProduct.isEmpty()) {
            log.error("No products found in database");
            throw new NotFoundException("No products found in database");
        }

        return ProductMapper.INSTANCE.toDTOList(listProduct);
    }

    public ProductDTO save(ProductDTO productDTO) {
        log.info("Saving product in database");
        validateNameAndPriceProduct(productDTO.getName(), productDTO.getPrice(), productDTO.getCategoryId());

       Product product = productMapper.toEntity(productDTO);

       product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Updating id: {} in database", id);
        Product product = returnProduct(id);

        validateNameAndPriceProduct(productDTO.getName(), productDTO.getPrice(), productDTO.getCategoryId());

        updateProductDTO(product, productDTO);

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public String delete(Long id) {
        log.info("Deleting id: {} in database", id);
        if (!productRepository.existsById(id)){
            log.error("Product not found with id: {}",id);
            throw new NotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        return "Product " + id + " deleted with successfully";
    }

    private Product returnProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));
    }

    private void validateNameAndPriceProduct(String name, Double price, Long id) {
        if (name == null || name.isEmpty()) {
            log.error("Product name cannot be null our empty");
            throw new ArgumentException("Product name cannot be null our empty");
        }

        if (productRepository.existsByName(name)) {
            log.error("Product already exists");
            throw new DuplicateDataException("Product " + name + " already exists");
        }

        if (price == null || price < 0) {
            log.error("Product price cannot be null our negative");
            throw new ArgumentException("Product price cannot be null our negative");
        }

        if (id == null) {
            log.error("Category Id cannot be null");
            throw new ArgumentException("Category Id cannot be null");
        }
    }

}
