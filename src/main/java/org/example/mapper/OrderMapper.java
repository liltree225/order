package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.Order;
import org.example.domain.OrderItem;
import org.example.dto.CreateOrderItemRequestDto;
import org.example.dto.CreateOrderRequestDto;
import org.example.dto.OrderResponseDto;
import org.example.enumeration.OrderStatus;
import org.springframework.stereotype.Component;

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
        order.setStatus(OrderStatus.ORDER_CREATED);

        // TODO: несостыковка — totalAmountOrder имеет тип Long, а itemDto.getPrice() может быть дробным
        //  (в БД DECIMAL(10,2)). При .longValue() дробная часть отбрасывается — потеря точности.
        //  Также item.getUnitPrice() * item.getQuantity() может переполнить Long при больших значениях.
        Long totalAmountOrder = 0L;



       if(dto.getItems() != null){
           for (CreateOrderItemRequestDto itemDto : dto.getItems()){
               OrderItem item = new OrderItem();
               item.setProductId(itemDto.getProductId());
               item.setQuantity(itemDto.getQuantity());
               // TODO: несостыковка — productName захардкожен как "Товар #" + productId.
               //  В CreateOrderItemRequestDto нет поля productName (есть todo добавить),
               //  а в БД product_name NOT NULL. Нужно передавать реальное имя товара от клиента
               item.setProductName("Товар #" + itemDto.getProductId());
               item.setUnitPrice(itemDto.getPrice().longValue());
               // TODO: несостыковка — setTotalPrice дублирует логику OrderItem.calculateTotalPrice(),
               //  который уже вызывается внутри setUnitPrice(). Двойной расчёт. Убрать явный setTotalPrice.
               item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
               order.addItem(item);
               totalAmountOrder = totalAmountOrder + item.getTotalPrice();
           }
       }

        order.setTotalAmount(totalAmountOrder);


        return order;
    }

}
