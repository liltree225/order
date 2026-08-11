package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequestDto {
    @NotNull
    private Long productId;
    private String productName;



    @Positive
    private Integer quantity;
    @Positive
    private BigDecimal price;
}
