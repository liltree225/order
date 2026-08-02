package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.PaymentMethod;
// TODO: несостыковка — импорт PaymentStatus не используется. Убрать.
import org.example.enumeration.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;
}
