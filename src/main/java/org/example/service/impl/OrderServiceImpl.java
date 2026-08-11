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

    private Payment createPayment(Order order, PaymentRequestDto requestDto) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod().name());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        return payment;
    }


    private Order getOrderOrThrow(Long id) {
        return orderDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден"));
    }

    private void sendNotification(Order savedOrder, String eventType, String subject, String message, @Nullable String reason) {
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();
        notificationSendRequest.setOrderId(savedOrder.getId());
        notificationSendRequest.setUserId(savedOrder.getUserId());
        notificationSendRequest.setUserEmail(savedOrder.getUserEmail());
        notificationSendRequest.setEventType(eventType);
        notificationSendRequest.setSubject(subject);
        notificationSendRequest.setMessage(message);
        notificationSendRequest.setTotalAmount(savedOrder.getTotalAmount());
        notificationSendRequest.setReason(reason);
        try {
            notificationFeignClient.sendNotification(notificationSendRequest);
        } catch (Exception e) {

            System.err.println("Ошибка при отправке уведомления: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto) {
        Order order = orderMapper.toEntity(createOrderRequestDto);
        Order savedOrder = orderDao.save(order);
        sendNotification(savedOrder, "ORDER_CREATED", "Создание заказа №" + savedOrder.getId(), "Ваш заказ на сумму " + savedOrder.getTotalAmount() + " успешно создан.", null);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        return orderMapper.toDto(getOrderOrThrow(id));
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
        Order order = getOrderOrThrow(orderId);
        String eventType;

        if (order.getStatus().name().equals("ORDER_CREATED") && requestDto.getNewStatus().name().equals("ORDER_CANCELLED")) {
            order.setStatus(OrderStatus.ORDER_CANCELLED);
            eventType = "ORDER_CANCELLED";
        } else if (order.getStatus().name().equals("ORDER_PAID") && requestDto.getNewStatus().name().equals("ORDER_SHIPPED")) {
            order.setStatus(OrderStatus.ORDER_SHIPPED);
            eventType = "ORDER_SHIPPED";
        } else if (order.getStatus().name().equals("ORDER_PAID") && requestDto.getNewStatus().name().equals("ORDER_CANCELLED")) {
            order.setStatus(OrderStatus.ORDER_CANCELLED);
            eventType = "ORDER_CANCELLED";
        } else if (order.getStatus().name().equals("ORDER_SHIPPED") && requestDto.getNewStatus().name().equals("ORDER_DELIVERED")) {
            order.setStatus(OrderStatus.ORDER_DELIVERED);
            eventType = "ORDER_DELIVERED";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus() + " в " + requestDto.getNewStatus()
            );
        }
        orderDao.save(order);
        sendNotification(order, eventType, "Новый статус заказа №" + order.getId(), "Ваш статус заказа успешно изменен", null);


        return orderMapper.toDto(order);
    }

    @Transactional
    @Override
    public PaymentResponseDto payOrder(Long id, PaymentRequestDto requestDto) {
        Order order = getOrderOrThrow(id);
        if (!order.getStatus().name().equals("ORDER_CREATED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus()
            );
        }
        if (paymentDao.findByOrderId(id) != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Платеж уже существует"
            );
        }
        Payment payment = createPayment(order, requestDto);

        order.setStatus(OrderStatus.ORDER_PAID);
        orderDao.save(order);
        paymentDao.save(payment);
        sendNotification(order, "ORDER_PAID", "Оплата заказа №" + order.getId(), "Ваш заказ на сумму " + order.getTotalAmount() + " успешно оплачен.", null);

        return paymentMapper.toDto(payment);
    }


    @Transactional
    @Override
    public OrderResponseDto deleteOrder(Long id, @Nullable CancelOrderRequestDto requestDto) {
        Order order = getOrderOrThrow(id);
        Payment payment = paymentDao.findByOrderId(id);

        if (!order.getStatus().name().equals("ORDER_CREATED") && !order.getStatus().name().equals("ORDER_PAID")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимый переход статуса из " + order.getStatus()
            );
        }

        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentDao.save(payment);
        }

        order.setStatus(OrderStatus.ORDER_CANCELLED);
        orderDao.save(order);
        sendNotification(order, "ORDER_CANCELLED", "Отмена заказа №" + order.getId(), "Ваш заказ успешно отменен", requestDto.getReason());


        return orderMapper.toDto(order);
    }


}
