package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.PaymentMethod;
import org.example.enumeration.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;
}
