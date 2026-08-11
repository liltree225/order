package org.example.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.example.dto.NotificationSendRequestDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class NotificationProducer {
    private final static String topic = "notification";
    private final KafkaTemplate<String, NotificationSendRequestDto> kafkaTemplate;
    public void sendNotification(NotificationSendRequestDto requestDto){
        try {
            kafkaTemplate.send(topic,requestDto.getOrderId().toString(), requestDto);
        }catch (Exception e){
            throw e;
        }
    }
}
