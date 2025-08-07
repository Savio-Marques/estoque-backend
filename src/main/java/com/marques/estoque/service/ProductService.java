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

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    private final ProductMapper productMapper;


    public ProductDTO findById(Long id) {
        log.info("Buscando produto por id");
        return ProductMapper.INSTANCE.toDTO(returnProductWithId(id));
    }

    public ProductDTO findByName(String name) {
        log.info("Buscando produto por nome");
        return ProductMapper.INSTANCE.toDTO(returnProductWithName(name));
    }

    public List<ProductDTO> findAll() {
        log.info("Buscando todos os produtos");
        List<Product> listProduct = productRepository.findAll();

        if(listProduct.isEmpty()) {
            log.error("Nenhum produto encontrado");
            throw new NotFoundException("Nenhum produto encontrado");
        }

        return ProductMapper.INSTANCE.toDTOList(listProduct);
    }

    public ProductDTO save(ProductDTO productDTO) {
        log.info("Salvando produto no banco de dados");
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
        log.info("Atualizando id: {} no banco de dados", id);
        Product product = returnProductWithId(id);

        validateProduct(productDTO.getName(), productDTO.getQtd() ,productDTO.getCategoryId());

        updateProductDTO(product, productDTO);

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public ProductDTO updatePatch(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {} no banco de dados", id);

        Product product = returnProductWithId(id);

        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getQtd() != null) {
            product.setQtd(productDTO.getQtd());
        }
        if (productDTO.getCategoryId() != null) {
            product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));
        }

        return ProductMapper.INSTANCE.toDTO(productRepository.save(product));
    }

    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        if (!productRepository.existsById(id)){
            log.error("Produto não encontrado com o id: {}",id);
            throw new NotFoundException("Produto com o id " + id + " não encontrado");
        }

        productRepository.deleteById(id);
        return "Produto com id " + id + " deletado com sucesso";
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private Product returnProductWithId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto com o id: " + id + " não encontrado"));
    }

    private Product returnProductWithName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Produto com o nome: "+ name + " não encontrado"));
    }

    private void updateProductDTO(Product product, ProductDTO productDTO) {
        product.setName(productDTO.getName());
        product.setQtd(productDTO.getQtd());
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId()));
    }

    private void validateProduct(String name, Integer qtd ,Long id) {
        if (name == null || name.isEmpty()) {
            log.error("O nome do produto não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (qtd == null) {
            log.error("A quantidade do produto não pode ser nula");
            throw new ArgumentException("A quantidade do produto não pode ser nulo(a)");
        }

        if (id == null) {
            log.error("O Id da categoria não pode ser nulo");
            throw new ArgumentException("O Id da categoria não pode ser nulo(a)");
        }
    }

}
