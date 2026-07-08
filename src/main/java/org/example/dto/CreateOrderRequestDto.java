package org.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {
    @NotNull
    private Long userId;
    @NotBlank
    private String userEmail;
    @NotBlank
    private String shippingAddress;
    @NotEmpty
    @Valid
    private List<OrderItemDto> items;
}
