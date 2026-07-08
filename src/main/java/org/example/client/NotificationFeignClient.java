package org.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "notification-service", url = "http://localhost:5124")

public interface NotificationFeignClient {
    @GetMapping("api/v1/notification/hello")
    void hello();
}
