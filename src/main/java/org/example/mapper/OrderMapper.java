package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.Order;
import org.example.domain.OrderItem;
import org.example.dto.CreateOrderItemRequestDto;
import org.example.dto.CreateOrderRequestDto;
import org.example.dto.OrderDto;
import org.example.dto.OrderResponseDto;
import org.example.enumeration.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
@RequiredArgsConstructor
@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderResponseDto toDto(Order order){
        return new OrderResponseDto(
                order.getId(),
                order.getUserId(),
                order.getUserEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getItems().stream()
                        .map(orderItemMapper::toDto)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public Order toEntity(CreateOrderRequestDto dto){
        if (dto == null){
            return null;
        }

        Order order = new Order();

        order.setUserId(dto.getUserId());
        order.setUserEmail(dto.getUserEmail());
        order.setShippingAddress(dto.getShippingAddress());
        order.setStatus(OrderStatus.CREATED);

        Long totalAmountOrder = 0L;



       if(dto.getItems() != null){
           for (CreateOrderItemRequestDto itemDto : dto.getItems()){
               OrderItem item = new OrderItem();
               item.setProductId(itemDto.getProductId());
               item.setQuantity(itemDto.getQuantity());
               item.setProductName("Товар #" + itemDto.getProductId());
               item.setUnitPrice(10000L);
               order.addItem(item);
               totalAmountOrder = totalAmountOrder + item.getTotalPrice();
           }
       }

        order.setTotalAmount(totalAmountOrder);


        return order;
    }

}
