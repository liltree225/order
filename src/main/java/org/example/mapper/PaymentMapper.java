package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.Payment;
import org.example.dto.PaymentResponseDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentMapper {

    public PaymentResponseDto toDto(Payment payment){
        return new PaymentResponseDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getPaidAt()
        );
    }
}
