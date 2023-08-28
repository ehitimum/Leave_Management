package com.example.leave_management.dto.RequestAndResponseDTO.UpdateAccount;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserNameReq {
    @NotNull
    private String userName;
}
