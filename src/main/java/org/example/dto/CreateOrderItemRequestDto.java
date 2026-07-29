package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequestDto {
    @NotNull
    private Long productId;
    @Positive
    private Integer quantity;
    @Positive
    private Integer price;
}
