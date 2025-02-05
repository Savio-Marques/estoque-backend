package com.marques.estoque.service;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.Product;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.util.ProductMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductMapper productMapper;


    public ProductDTO findById(Long id) {
        return ProductMapper.INSTANCE.toDTO(returnProduct(id));
    }

    public List<ProductDTO> findAll() {
        return ProductMapper.INSTANCE.toDTOList(productRepository.findAll());
    }

    public ProductDTO save(ProductDTO productDTO) {
       Product product = productMapper.toEntity(productDTO);

        product.setCategories(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO productDTO) {

        Product product = returnProduct(id);
        updateProductDTO(product, productDTO);

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public String delete(Long id) {
        productRepository.deleteById(id);
        return "Product" + id + "deleted";
    }

    private Product returnProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setCategories(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
    }
}
