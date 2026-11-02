package com.example.spotlight.controller;

import com.example.spotlight.dto.SpotlightRequest;
import com.example.spotlight.dto.SpotlightResponse;
import com.example.spotlight.service.application.ApplySpotlightService;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spotlights")
public class SpotlightController {

    private final ApplySpotlightService service;

    public SpotlightController(ApplySpotlightService service) {
        this.service = service;
    }

    @Observed(name = "apply.spotlights")
    @PostMapping("/{ownerId}")
    public ResponseEntity<SpotlightResponse> apply(
            @PathVariable Long ownerId,
            @RequestBody @Valid SpotlightRequest request) {
        var response = service.applySpotlight(request, ownerId);
        return ResponseEntity.ok(response);
    }
}
