package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("api/v1/order")
@RequiredArgsConstructor

public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;



    @PostMapping
    public OrderResponseDto createOrder(@Valid @RequestBody CreateOrderRequestDto createOrderRequestDto){
        return orderService.createOrder(createOrderRequestDto);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrderById(@PathVariable Long id){
        return orderService.getOrderById(id);
    }

    @GetMapping
    public OrderListResponseDto getOrdersByUserId(@RequestParam Long userId){
        return orderService.getOrdersByUserId(userId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponseDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequestDto requestDto){
        return orderService.updateStatus(id,requestDto);
    }

    @PostMapping("/{id}/pay")
    public PaymentResponseDto payOrder (@PathVariable Long id, @Valid @RequestBody PaymentRequestDto requestDto){
        return orderService.payOrder(id, requestDto);
    }

    @DeleteMapping("/{id}")
    public OrderResponseDto deleteOrder(@PathVariable Long id, @RequestBody(required = false)  CancelOrderRequestDto requestDto){
        return orderService.deleteOrder(id, requestDto);
    }



}
