package org.example.service.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.example.client.NotificationFeignClient;
import org.example.domain.Order;
import org.example.domain.Payment;
import org.example.dto.*;
import org.example.enumeration.OrderStatus;
import org.example.enumeration.PaymentStatus;
import org.example.mapper.OrderMapper;
import org.example.mapper.PaymentMapper;
import org.example.repository.OrderDao;
import org.example.repository.PaymentDao;
import org.example.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

    private final NotificationFeignClient notificationFeignClient;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final OrderDao orderDao;
    private final PaymentDao paymentDao;

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
        notificationSendRequest.setSubject("Создание заказа №" + savedOrder.getId());
        notificationSendRequest.setMessage("Ваш заказ на сумму " + savedOrder.getTotalAmount() + " успешно создан.");
        notificationSendRequest.setTotalAmount(savedOrder.getTotalAmount());
        notificationFeignClient.sendNotification(notificationSendRequest);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        return orderMapper.toDto(orderDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден")));
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

    @Override
    @Transactional
    public OrderResponseDto updateStatus(Long orderId, UpdateStatusRequestDto requestDto) {
        Order order = orderDao.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + orderId + " не найден"));
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();
        if (order.getStatus().name().equals("ORDER_CREATED") && requestDto.getNewStatus().name().equals("ORDER_CANCELLED")) {
            order.setStatus(OrderStatus.ORDER_CANCELLED);
            notificationSendRequest.setEventType("ORDER_CANCELLED");
        } else if (order.getStatus().name().equals("ORDER_CREATED") && requestDto.getNewStatus().name().equals("ORDER_PAID")) {
            order.setStatus(OrderStatus.ORDER_PAID);
            notificationSendRequest.setEventType("ORDER_PAID");
        } else if (order.getStatus().name().equals("ORDER_PAID") && requestDto.getNewStatus().name().equals("ORDER_SHIPPED")) {
            order.setStatus(OrderStatus.ORDER_SHIPPED);
            notificationSendRequest.setEventType("ORDER_SHIPPED");
        } else if (order.getStatus().name().equals("ORDER_PAID") && requestDto.getNewStatus().name().equals("ORDER_CANCELLED")) {
            order.setStatus(OrderStatus.ORDER_CANCELLED);
            notificationSendRequest.setEventType("ORDER_CANCELLED");
        } else if (order.getStatus().name().equals("ORDER_SHIPPED") && requestDto.getNewStatus().name().equals("ORDER_DELIVERED")) {
            order.setStatus(OrderStatus.ORDER_DELIVERED);
            notificationSendRequest.setEventType("ORDER_DELIVERED");
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus() + " в " + requestDto.getNewStatus()
            );
        }
        notificationSendRequest.setOrderId(order.getId());
        notificationSendRequest.setUserId(order.getUserId());
        notificationSendRequest.setUserEmail(order.getUserEmail());
        notificationSendRequest.setTotalAmount(order.getTotalAmount());
        orderDao.save(order);
        try {
            notificationFeignClient.sendNotification(notificationSendRequest);
        } catch (Exception e) {

            System.err.println("Ошибка при отправке уведомления: " + e.getMessage());
        }


        return orderMapper.toDto(order);
    }

    @Override
    public PaymentResponseDto payOrder(Long id, PaymentRequestDto requestDto) {
        Order order = orderDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден"));
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();
        if (!order.getStatus().name().equals("ORDER_CREATED")){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus()
            );
        }
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod().name());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        order.setStatus(OrderStatus.ORDER_PAID);
        orderDao.save(order);
        notificationSendRequest.setOrderId(order.getId());
        notificationSendRequest.setUserId(order.getUserId());
        notificationSendRequest.setUserEmail(order.getUserEmail());
        notificationSendRequest.setEventType("ORDER_PAID");
        notificationSendRequest.setTotalAmount(order.getTotalAmount());
        notificationFeignClient.sendNotification(notificationSendRequest);

        paymentDao.save(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    public OrderResponseDto deleteOrder(Long id, @Nullable CancelOrderRequestDto requestDto) {
        Order order = orderDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден"));
        Payment payment = paymentDao.findByOrderId(id);
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();

        if (!order.getStatus().name().equals("ORDER_CREATED") && !order.getStatus().name().equals("ORDER_PAID") ){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus()
            );
        }

        if(payment != null && payment.getStatus() == PaymentStatus.SUCCESS){
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentDao.save(payment);
        }

        order.setStatus(OrderStatus.ORDER_CANCELLED);
        orderDao.save(order);

        notificationSendRequest.setOrderId(order.getId());
        notificationSendRequest.setUserId(order.getUserId());
        notificationSendRequest.setUserEmail(order.getUserEmail());
        notificationSendRequest.setEventType("ORDER_CANCELLED");
        notificationSendRequest.setTotalAmount(order.getTotalAmount());
        if(requestDto != null){
            notificationSendRequest.setReason(requestDto.getReason());
        }
        notificationFeignClient.sendNotification(notificationSendRequest);

        return orderMapper.toDto(order);
    }


}
