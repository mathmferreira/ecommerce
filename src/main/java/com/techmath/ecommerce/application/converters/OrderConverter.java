package com.techmath.ecommerce.application.converters;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.presentation.dto.response.OrderResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OrderConverter implements Converter<Order, OrderResponse> {

    @Override
    public Order toEntity(OrderResponse orderResponse) {
        var entity = new Order();
        BeanUtils.copyProperties(orderResponse, entity);
        return entity;
    }

    @Override
    public OrderResponse toDTO(Order entity) {
        return new OrderResponse(entity.getId(), entity.getStatus(), null);
    }

}
