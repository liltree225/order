package org.example.service;

import org.example.dto.CreateOrderRequestDto;
import org.example.dto.OrderListResponseDto;
import org.example.dto.OrderResponseDto;

public interface OrderService {
    void hello();

    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto);
    public OrderResponseDto getOrderById(Long id);
    public OrderListResponseDto getOrdersByUserId(Long userId);
}
