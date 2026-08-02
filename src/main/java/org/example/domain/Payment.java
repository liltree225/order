package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    // TODO: несостыковка — amount имеет тип Long, но в init.sql amount DECIMAL(12,2).
    //  При сохранении дробного значения произойдёт потеря точности или ошибка.
    //  Нужно использовать BigDecimal.
    private Long amount;
    private String paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;



}
