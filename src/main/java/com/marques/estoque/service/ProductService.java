package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Product;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.util.ProductMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    private final ProductMapper productMapper;


    public ProductDTO findById(Long id) {
        log.info("Searching product by id");
        return ProductMapper.INSTANCE.toDTO(returnProductWithId(id));
    }

    public ProductDTO findByName(String name) {
        log.info("Searching product by name");

        return ProductMapper.INSTANCE.toDTO(returnProductWithName(name));
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
        validateProduct(productDTO.getName(), productDTO.getQtd() ,productDTO.getCategoryId());

        if (productRepository.existsByName(productDTO.getName())){
            Product product = returnProductWithName(productDTO.getName());
            product.setQtd(productDTO.getQtd() + product.getQtd());
            return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
        }

       Product product = productMapper.toEntity(productDTO);

       product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Updating id: {} in database", id);
        Product product = returnProductWithId(id);

        validateProduct(productDTO.getName(), productDTO.getQtd() ,productDTO.getCategoryId());

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

    private Product returnProductWithId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private Product returnProductWithName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Product not found with name: "+ name));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setName(productDTO.getName());
        product.setQtd(productDTO.getQtd());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));
    }

    private void validateProduct(String name, Integer qtd ,Long id) {
        if (name == null || name.isEmpty()) {
            log.error("Product name cannot be null our empty");
            throw new ArgumentException("Product name cannot be null our empty");
        }

        if (qtd == null) {
            log.error("Product quantity cannot be null our empty");
            throw new ArgumentException("Product quantity cannot be null our empty");
        }

        if (id == null) {
            log.error("Category Id cannot be null");
            throw new ArgumentException("Category Id cannot be null");
        }
    }

}
