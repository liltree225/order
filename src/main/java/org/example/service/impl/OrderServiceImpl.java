package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.NotificationFeignClient;
import org.example.domain.Order;
import org.example.dto.*;
import org.example.mapper.OrderMapper;
import org.example.repository.OrderDao;
import org.example.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

    private final NotificationFeignClient notificationFeignClient;
    private final OrderMapper orderMapper;
    private final OrderDao orderDao;

    @Override
    public void hello() {
        notificationFeignClient.hello();
    }


    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto) {

        Order order = orderMapper.toEntity(createOrderRequestDto);
        Order savedOrder = orderDao.save(order);
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();
        notificationSendRequest.setOrderId(savedOrder.getId());
        notificationSendRequest.setUserId(savedOrder.getUserId());
        notificationSendRequest.setUserEmail(savedOrder.getUserEmail());
        notificationSendRequest.setEventType("ORDER_CREATED");
        notificationSendRequest.setTotalAmount(savedOrder.getTotalAmount());
        notificationFeignClient.sendNotification(notificationSendRequest);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        return orderMapper.toDto(orderDao.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден")));
    }

    @Override
    public OrderListResponseDto getOrdersByUserId(Long userId) {
        OrderListResponseDto orderListResponseDto = new OrderListResponseDto();
        orderListResponseDto.setUserId(userId);
        List<Order> orderEntities = orderDao.findAllByUserId(userId);
        List<OrderResponseDto> orders = orderEntities.stream().map(orderMapper::toDto).toList();
        orderListResponseDto.setOrders(orders);
        orderListResponseDto.setTotalCount(orders.size());
        return orderListResponseDto;
    }


}
