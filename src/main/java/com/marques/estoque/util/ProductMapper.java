package com.marques.estoque.util;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.product.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "categories.id", target = "categoryId")
    @Mapping(source = "categories.name", target = "categoryName")
    @Mapping(target = "status", ignore = true)
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "user", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @AfterMapping
    default void calculateStatus(@MappingTarget ProductDTO dto, Product product) {
        if (product == null) {
            return;
        }

        int threshold = product.getMinQuantity() != null ? product.getMinQuantity() : 5;

        if (product.getQtd() == 0) {
            dto.setStatus("Sem Estoque");
        } else if (product.getQtd() <= threshold) {
            dto.setStatus("Estoque Baixo");
        } else {
            dto.setStatus("Disponível");
        }
    }
}
