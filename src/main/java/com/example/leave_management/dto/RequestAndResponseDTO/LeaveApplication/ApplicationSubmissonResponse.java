package com.example.leave_management.dto.RequestAndResponseDTO.LeaveApplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationSubmissonResponse {
    private String response;
}