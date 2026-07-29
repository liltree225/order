package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.OrderItem;
import org.example.dto.OrderItemDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderItemMapper {

    public OrderItemDto toDto(OrderItem orderItem){
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getTotalPrice(),
                orderItem.getOrder().getId()
        );
    }
}
