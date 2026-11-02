package com.example.spotlight.service.persistence;

import com.example.spotlight.domain.SpotlightLog;
import com.example.spotlight.exceptions.ResourceNotFoundException;
import com.example.spotlight.repository.SpotlightLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SpotlightPersistenceService {

    private final SpotlightLogRepository repository;

    public SpotlightPersistenceService(SpotlightLogRepository repository) {
        this.repository = repository;
    }

    public SpotlightLog save(SpotlightLog spotlight) {
        return repository.save(spotlight);
    }

    public SpotlightLog findByResourceCode(String resourceCode) {
        return repository.findByResourceCode(resourceCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Spotlight not found for resource code: " + resourceCode));
    }

    public List<SpotlightLog> findByOwnerId(Long ownerId) {
        return repository.findByOwnerId(ownerId);
    }
}
