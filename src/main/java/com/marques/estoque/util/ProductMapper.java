package com.marques.estoque.util;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "categories.id", target = "categoryId")
    ProductDTO toDTO(Product product);

    @Mapping(source = "categories.id", target = "categoryId")
    List<ProductDTO> toDTOList(List<Product> product);


    @Mapping(target = "categories", ignore = true) // A categoria será definida no service
    Product toEntity(ProductDTO dto);

}