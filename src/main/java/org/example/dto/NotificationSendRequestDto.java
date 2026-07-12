package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequestDto {

    private Long orderId;
    private Long userId;
    private String userEmail;
    private String eventType;
    private Long totalAmount;
    private String subject;
    private String message;
    private String trackingNumber;
    private String reason;
}
