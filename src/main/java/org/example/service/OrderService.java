package org.example.service;

import org.example.dto.*;

import java.util.Optional;

public interface OrderService {
    void hello();

    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto);
    public OrderResponseDto getOrderById(Long id);
    public OrderListResponseDto getOrdersByUserId(Long userId);
    public OrderResponseDto updateStatus(Long orderId, UpdateStatusRequestDto requestDto);
    public PaymentResponseDto payOrder(Long id, PaymentRequestDto requestDto);
    // TODO: несостыковка — в интерфейсе параметр CancelOrderRequestDto без @Nullable,
    //  а в имплементации — @Nullable. В контроллере @RequestBody(required = false).
    //  Нужно привести к единому контракту.
    public OrderResponseDto deleteOrder(Long id, CancelOrderRequestDto requestDto);
}
