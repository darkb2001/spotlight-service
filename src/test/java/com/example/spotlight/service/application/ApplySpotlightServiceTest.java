package com.example.spotlight.service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spotlight.domain.SpotlightLog;
import com.example.spotlight.dto.SpotlightRequest;
import com.example.spotlight.mapper.SpotlightMapper;
import com.example.spotlight.metrics.SpotlightMetrics;
import com.example.spotlight.service.domain.SpotlightDomainService;
import com.example.spotlight.service.persistence.SpotlightPersistenceService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplySpotlightServiceTest {

    @Mock
    private SpotlightDomainService domainService;

    @Mock
    private SpotlightPersistenceService persistenceService;

    @Mock
    private SpotlightMapper mapper;

    @Mock
    private SpotlightMetrics metrics;

    @InjectMocks
    private ApplySpotlightService service;

    @Test
    void shouldApplySpotlight() {
        var request = SpotlightRequest.builder()
                .resourceCode("RES-001")
                .highlights(List.of("Premium", "Featured"))
                .position(1)
                .build();

        var calculated = SpotlightLog.builder()
                .ownerId(100L)
                .resourceCode("RES-001")
                .highlights("Premium,Featured")
                .position(1)
                .build();

        var saved = SpotlightLog.builder()
                .id(1L)
                .ownerId(100L)
                .resourceCode("RES-001")
                .highlights("Premium,Featured")
                .position(1)
                .appliedDate(LocalDateTime.now())
                .build();

        when(domainService.calculateSpotlight(100L, request)).thenReturn(calculated);
        when(persistenceService.save(any(SpotlightLog.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(
                com.example.spotlight.dto.SpotlightResponse.builder()
                        .id(1L)
                        .ownerId(100L)
                        .resourceCode("RES-001")
                        .highlights(List.of("Premium", "Featured"))
                        .position(1)
                        .appliedDate(saved.getAppliedDate())
                        .build());

        var result = service.applySpotlight(request, 100L);

        assertNotNull(result);
        assertEquals("RES-001", result.getResourceCode());
        assertEquals(100L, result.getOwnerId());
        verify(persistenceService).save(any(SpotlightLog.class));
        verify(metrics).recordApplied();
    }
}
