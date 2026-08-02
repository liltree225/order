package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.OrderStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// TODO: несостыковка — импорт java.awt.* удалён (был неиспользуемый). BigDecimal тоже не используется — убрать.

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String userEmail;
    // TODO: несостыковка — totalAmount имеет тип Long, но в init.sql order_items.unit_price/total_price
    //  имеют тип DECIMAL(10,2). При дробных ценах произойдёт потеря точности.
    //  Решить: либо BigDecimal везде, либо BIGINT в БД.
    private Long totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String shippingAddress;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    // TODO: несостыковка — @CreatedDate и @LastModifiedDate требуют @EntityListeners(AuditingEntityListener.class)
    //  на классе и @EnableJpaAuditing на конфигурации. Без них аннотации игнорируются,
    //  а значения всегда выставляются через LocalDateTime.now() при создании объекта в памяти,
    //  но не обновляются при save. Проверить, включён ли аудит.
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);

        item.setOrder(this);
    }
}
