package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.NotificationFeignClient;
import org.example.service.OrderService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

    private final NotificationFeignClient notificationFeignClient;


    @Override
    public void hello() {
        notificationFeignClient.hello();
    }
}
