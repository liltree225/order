package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponseDto {
    private Long userId;
    private List<OrderDto> orders;
    private Integer totalCount;
}
