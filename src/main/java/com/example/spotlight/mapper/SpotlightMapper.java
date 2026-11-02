package com.example.spotlight.mapper;

import com.example.spotlight.domain.SpotlightLog;
import com.example.spotlight.dto.SpotlightResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SpotlightMapper {

    public SpotlightResponse toResponse(SpotlightLog entity) {
        return SpotlightResponse.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .resourceCode(entity.getResourceCode())
                .highlights(splitHighlights(entity.getHighlights()))
                .position(entity.getPosition())
                .appliedDate(entity.getAppliedDate())
                .build();
    }

    public String joinHighlights(List<String> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return "";
        }
        return String.join(",", highlights);
    }

    private List<String> splitHighlights(String highlights) {
        if (highlights == null || highlights.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(highlights.split(","));
    }
}
