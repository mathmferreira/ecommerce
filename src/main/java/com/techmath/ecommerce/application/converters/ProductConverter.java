package com.techmath.ecommerce.application.converters;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.presentation.dto.ProductDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter implements Converter<Product, ProductDTO> {

    @Override
    public Product toEntity(ProductDTO dto) {
        var entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public ProductDTO toDTO(Product entity) {
        return new ProductDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getStockQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
