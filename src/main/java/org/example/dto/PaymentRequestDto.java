package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.PaymentMethod;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;
}
