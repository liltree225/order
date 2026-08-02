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

    // TODO: рефакторинг — вынести отправку уведомления в приватный метод.
    //  Создание NotificationSendRequestDto и вызов notificationFeignClient.sendNotification
    //  дублируется в createOrder, updateStatus, payOrder, deleteOrder.
    //  Например: private void sendNotification(Order order, String eventType, String subject, String message)
    // TODO: рефакторинг — вынести загрузку заказа по ID в приватный метод.
    //  orderDao.findById(id).orElseThrow(...404...) дублируется в getOrderById, updateStatus, payOrder, deleteOrder.
    //  Например: private Order getOrderOrThrow(Long id)

    @Override
    public void hello() {
        notificationFeignClient.hello();
    }


    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto) {

        Order order = orderMapper.toEntity(createOrderRequestDto);
        Order savedOrder = orderDao.save(order);
        // TODO: несостыковка — нет try-catch вокруг sendNotification, в отличие от updateStatus.
        //  Если notification-service недоступен, весь createOrder упадёт, и заказ не будет создан для клиента
        //  (хотя в БД он уже сохранён). Нужно обернуть в try-catch или вынести отправку уведомлений
        //  в асинхронный/outbox-механизм.
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
        // TODO: несостыковка — updateStatus разрешает переход ORDER_CREATED -> ORDER_PAID (строка ниже),
        //  но при этом не создаётся запись Payment. Заказ в статусе ORDER_PAID остаётся без платежа,
        //  и deleteOrder не сможет оформить возврат (paymentDao.findByOrderId вернёт null).
        //  Нужно либо запретить переход в ORDER_PAID через updateStatus (только через payOrder),
        //  либо создавать Payment внутри updateStatus.
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

    // TODO: несостыковка — метод не аннотирован @Transactional. orderDao.save и paymentDao.save
    //  выполняются в разных транзакциях. Если paymentDao.save упадёт, заказ уже будет ORDER_PAID без платежа.
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
        // TODO: несостыковка — нет проверки на существующий платёж. Если вызвать payOrder дважды,
        //  вторая попытка упадёт на UNIQUE-ограничении БД (payments.order_id) с необработанным исключением.
        //  Нужно проверять paymentDao.findByOrderId(id) != null и возвращать ошибку.
        // TODO: рефакторинг — вынести создание платежа в приватный метод.
        //  Например: private Payment createPayment(Order order, PaymentRequestDto requestDto)
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod().name());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        order.setStatus(OrderStatus.ORDER_PAID);
        orderDao.save(order);
        // TODO: несостыковка — уведомление отправляется ДО сохранения платежа (paymentDao.save ниже).
        //  Если paymentDao.save упадёт (а @Transactional нет), клиент получит ложное уведомление об оплате.
        //  Нужно сначала сохранить платёж, потом отправлять уведомление.
        // TODO: несостыковка — нет try-catch вокруг sendNotification, в отличие от updateStatus.
        //  Если notification-service недоступен, весь payOrder упадёт, и заказ останется без статуса.
        notificationSendRequest.setOrderId(order.getId());
        notificationSendRequest.setUserId(order.getUserId());
        notificationSendRequest.setUserEmail(order.getUserEmail());
        notificationSendRequest.setEventType("ORDER_PAID");
        notificationSendRequest.setTotalAmount(order.getTotalAmount());
        notificationFeignClient.sendNotification(notificationSendRequest);

        paymentDao.save(payment);

        return paymentMapper.toDto(payment);
    }

    // TODO: несостыковка — метод не аннотирован @Transactional. Изменение статуса платежа,
    //  изменение статуса заказа и отправка уведомления выполняются без атомарности.
    // TODO: несостыковка — нет try-catch вокруг sendNotification, в отличие от updateStatus.
    @Override
    public OrderResponseDto deleteOrder(Long id, @Nullable CancelOrderRequestDto requestDto) {
        Order order = orderDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ с ID " + id + " не найден"));
        Payment payment = paymentDao.findByOrderId(id);
        NotificationSendRequestDto notificationSendRequest = new NotificationSendRequestDto();

        // TODO: несостыковка — cancel через deleteOrder не требует reason, а через updateStatus
        //  с ORDER_CANCELLED — требует (см. @AssertTrue в UpdateStatusRequestDto).
        //  Нужно добавить валидацию reason и в CancelOrderRequestDto.
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
