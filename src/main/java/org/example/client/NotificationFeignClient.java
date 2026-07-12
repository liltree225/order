package org.example.client;

import org.example.dto.NotificationResponseDto;
import org.example.dto.NotificationSendRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:5124")

public interface NotificationFeignClient {
    @GetMapping("api/v1/notification/hello")
    void hello();

    @PostMapping("api/v1/notification/sent")
    NotificationResponseDto sendNotification(@RequestBody NotificationSendRequestDto request);
}
