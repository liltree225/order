package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Long unitPrice;
    private Long totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    private void calculateTotalPrice(){
        if (this.quantity != null && this.unitPrice != null){
            this.totalPrice = this.unitPrice * this.quantity;
        }
    }
}
