package com.ifpb.charger_manager.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String cpfCnpj;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
