package com.example.spotlight.service.domain;

import com.example.spotlight.domain.SpotlightLog;
import com.example.spotlight.dto.SpotlightRequest;
import com.example.spotlight.mapper.SpotlightMapper;
import org.springframework.stereotype.Service;

@Service
public class SpotlightDomainService {

    private final SpotlightMapper mapper;

    public SpotlightDomainService(SpotlightMapper mapper) {
        this.mapper = mapper;
    }

    public SpotlightLog calculateSpotlight(Long ownerId, SpotlightRequest request) {
        int position = request.getPosition() != null ? request.getPosition() : 1;
        return SpotlightLog.builder()
                .ownerId(ownerId)
                .resourceCode(request.getResourceCode())
                .highlights(mapper.joinHighlights(request.getHighlights()))
                .position(position)
                .build();
    }
}
