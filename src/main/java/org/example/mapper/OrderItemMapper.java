package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderItemDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderItemMapper {

    public OrderItemDto toDto(){
        return new OrderItemDto(

        );
    }
}
