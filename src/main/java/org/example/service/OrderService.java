package org.example.service;

import jakarta.annotation.Nullable;
import org.example.dto.*;

import java.util.Optional;

public interface OrderService {


    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto);
    public OrderResponseDto getOrderById(Long id);
    public OrderListResponseDto getOrdersByUserId(Long userId);
    public OrderResponseDto updateStatus(Long orderId, UpdateStatusRequestDto requestDto);
    public PaymentResponseDto payOrder(Long id, PaymentRequestDto requestDto);
    public OrderResponseDto deleteOrder(Long id, @Nullable CancelOrderRequestDto requestDto);
}
