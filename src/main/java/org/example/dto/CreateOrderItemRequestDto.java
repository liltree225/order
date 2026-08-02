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

    //todo Добавим productName

    @Positive
    private Integer quantity;
    // TODO: несостыковка — price имеет тип Integer, но в БД unit_price DECIMAL(10,2).
    //  Дробные цены (например 99.99) не пройдут валидацию или будут округлены.
    //  Нужно использовать BigDecimal или Double.
    @Positive
    private Integer price;
}
