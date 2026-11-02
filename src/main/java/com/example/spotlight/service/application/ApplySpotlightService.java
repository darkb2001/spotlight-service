package com.example.spotlight.service.application;

import com.example.spotlight.dto.SpotlightRequest;
import com.example.spotlight.dto.SpotlightResponse;
import com.example.spotlight.mapper.SpotlightMapper;
import com.example.spotlight.metrics.SpotlightMetrics;
import com.example.spotlight.service.domain.SpotlightDomainService;
import com.example.spotlight.service.persistence.SpotlightPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplySpotlightService {

    private static final Logger log = LoggerFactory.getLogger(ApplySpotlightService.class);

    private final SpotlightDomainService domainService;
    private final SpotlightPersistenceService persistenceService;
    private final SpotlightMapper mapper;
    private final SpotlightMetrics metrics;

    public ApplySpotlightService(
            SpotlightDomainService domainService,
            SpotlightPersistenceService persistenceService,
            SpotlightMapper mapper,
            SpotlightMetrics metrics) {
        this.domainService = domainService;
        this.persistenceService = persistenceService;
        this.mapper = mapper;
        this.metrics = metrics;
    }

    public SpotlightResponse applySpotlight(SpotlightRequest request, Long ownerId) {
        log.info("Applying spotlight for ownerId={} resourceCode={}", ownerId, request.getResourceCode());

        var spotlight = domainService.calculateSpotlight(ownerId, request);
        var saved = persistenceService.save(spotlight);
        metrics.recordApplied();

        log.info("Successfully applied spotlight id={} for resourceCode={}",
                saved.getId(), saved.getResourceCode());
        return mapper.toResponse(saved);
    }
}
