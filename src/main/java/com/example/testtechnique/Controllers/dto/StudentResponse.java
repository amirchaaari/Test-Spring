package com.example.testtechnique.Controllers.dto;

import com.example.testtechnique.entities.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String username;
    private Level level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

