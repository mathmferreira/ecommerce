package com.techmath.ecommerce.application.converters;

public interface Converter<T, DTO> {

    T toEntity(DTO dto);

    DTO toDTO(T entity);

}
