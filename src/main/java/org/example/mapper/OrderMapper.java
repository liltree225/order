package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.Order;
import org.example.domain.OrderItem;
import org.example.dto.OrderDto;
import org.springframework.stereotype.Component;

import java.util.List;
@RequiredArgsConstructor
@Component
public class OrderMapper {

    public OrderDto toDto(Order order){
        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getUserEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getItems(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

}
