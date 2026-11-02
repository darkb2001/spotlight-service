package com.example.spotlight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotlightRequest {

    @NotBlank(message = "Resource code is required")
    private String resourceCode;

    @NotEmpty(message = "At least one highlight required")
    private List<String> highlights;

    @Min(value = 1, message = "Position must be positive")
    private Integer position;
}
