package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/order")
@RequiredArgsConstructor

public class OrderController {

    private final OrderService orderService;
    @GetMapping("/hello")
    void hello(){
        orderService.hello();
    }

}
