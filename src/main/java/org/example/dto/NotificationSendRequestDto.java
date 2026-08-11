package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequestDto {

    private Long orderId;
    private Long userId;
    private String userEmail;
    @JsonProperty("type")
    private String eventType;
    private BigDecimal totalAmount;
    private String subject;
    private String message;
    private String trackingNumber;
    private String reason;
}
