package org.example.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequestDto {
    private String reason;

    @AssertTrue(message = "Reason is required")
    public boolean isReasonValid() {
        return this.reason != null && !this.reason.isBlank();
    }
}
