package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("type")
    private String eventType;
    private Long totalAmount;
    private String subject;
    private String message;
    private String trackingNumber;
    private String reason;
}
