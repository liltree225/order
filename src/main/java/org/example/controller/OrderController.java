package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.CreateOrderRequestDto;
import org.example.dto.OrderListResponseDto;
import org.example.dto.OrderResponseDto;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/order")
@RequiredArgsConstructor

public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    @GetMapping("/hello")
    void hello(){
        orderService.hello();
    }

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

    @PatchMapping()


}
