package com.example.spotlight.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotlightResponse {

    private Long id;
    private Long ownerId;
    private String resourceCode;
    private List<String> highlights;
    private Integer position;
    private LocalDateTime appliedDate;
}
