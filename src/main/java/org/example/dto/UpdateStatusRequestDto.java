package org.example.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {
    @NotNull
    private OrderStatus newStatus;

    private String reason;

    @AssertTrue
    public boolean isReasonValid(){
        if (this.newStatus == OrderStatus.CANCELLED){
            return this.reason != null && !this.reason.isBlank();
        }
        return true;
    }
}
